//! C ABI bindings for the qppocr OCR engine.
//!
//! 所有返回的 `char *` JSON 字符串由 Rust 分配，调用方必须用
//! `qppocr_free_string` 释放。错误信息通过 `qppocr_last_error` 读取
//! （线程本地，只读，无需释放）。

use std::cell::RefCell;
use std::ffi::{c_char, c_int, c_uchar, CStr, CString};
use std::panic::{catch_unwind, AssertUnwindSafe};
use std::ptr;

use qppocr::{DeviceChoice, Engine, EngineBuilder, GpuApi, Preset, Tier};

thread_local! {
    static LAST_ERROR: RefCell<Option<CString>> = const { RefCell::new(None) };
}

fn set_last_error(msg: impl Into<String>) {
    let msg = msg.into().replace('\0', " ");
    LAST_ERROR.with(|e| *e.borrow_mut() = CString::new(msg).ok());
}

fn clear_last_error() {
    LAST_ERROR.with(|e| *e.borrow_mut() = None);
}

/// 读取当前线程最近一次失败的错误信息（UTF-8，只读，生命周期到下一次 API 调用）。
#[no_mangle]
pub extern "C" fn qppocr_last_error() -> *const c_char {
    LAST_ERROR.with(|e| match e.borrow().as_ref() {
        Some(s) => s.as_ptr(),
        None => ptr::null(),
    })
}

/// qppocr 引擎版本号（静态字符串，无需释放）。
#[no_mangle]
pub extern "C" fn qppocr_version() -> *const c_char {
    concat!(env!("CARGO_PKG_VERSION"), "\0").as_ptr() as *const c_char
}

unsafe fn cstr_opt<'a>(p: *const c_char) -> Option<&'a str> {
    if p.is_null() {
        return None;
    }
    // SAFETY: 调用方保证传入以 NUL 结尾的有效 C 字符串。
    unsafe { CStr::from_ptr(p) }.to_str().ok()
}

/// 创建 OCR 引擎实例。
///
/// - `models_dir`: 模型目录（必需），布局见 qppocr 文档
/// - `tier`: "tiny" / "small" / "medium"，NULL 表示 "small"
/// - `preset`: "speed" / "balanced" / "accuracy"，NULL 表示默认（Balanced）
/// - `threads`: <=0 表示自动
/// - `detect_orientation`: -1 默认，0 关，1 开
/// - `verify_sha256`: 0 跳过模型哈希校验，非 0 校验（默认）
///
/// 失败返回 NULL，错误信息见 `qppocr_last_error`。
#[no_mangle]
pub unsafe extern "C" fn qppocr_create(
    models_dir: *const c_char,
    tier: *const c_char,
    preset: *const c_char,
    threads: c_int,
    detect_orientation: c_int,
    verify_sha256: c_int,
    device: *const c_char,
) -> *mut Engine {
    clear_last_error();
    // SAFETY: catch_unwind 保证 panic 不越过 FFI 边界。
    let r = catch_unwind(AssertUnwindSafe(|| {
        let Some(dir) = (unsafe { cstr_opt(models_dir) }) else {
            set_last_error("models_dir 不能为空或不是有效 UTF-8");
            return ptr::null_mut();
        };
        let tier = match unsafe { cstr_opt(tier) }.unwrap_or("small") {
            "tiny" => Tier::Tiny,
            "small" => Tier::Small,
            "medium" => Tier::Medium,
            other => {
                set_last_error(format!("未知 tier: {other}（可选 tiny/small/medium）"));
                return ptr::null_mut();
            }
        };
        let preset = match unsafe { cstr_opt(preset) } {
            None => None,
            Some("speed") => Some(Preset::Speed),
            Some("balanced") => Some(Preset::Balanced),
            Some("accuracy") => Some(Preset::Accuracy),
            Some(other) => {
                set_last_error(format!("未知 preset: {other}（可选 speed/balanced/accuracy）"));
                return ptr::null_mut();
            }
        };

        let device = match unsafe { cstr_opt(device) } {
            None | Some("") | Some("cpu") => DeviceChoice::Cpu,
            Some("gpu") => DeviceChoice::gpu(),
            Some(s) if s == "vulkan" || s.starts_with("vulkan:") => {
                let index = match s.strip_prefix("vulkan:") {
                    None => None,
                    Some(i) => match i.parse::<u32>() {
                        Ok(v) => Some(v),
                        Err(_) => {
                            set_last_error(format!("非法设备序号: {i}"));
                            return ptr::null_mut();
                        }
                    },
                };
                DeviceChoice::Gpu { api: GpuApi::Vulkan, index }
            }
            Some("cuda") => DeviceChoice::Gpu { api: GpuApi::Cuda, index: None },
            Some(other) => {
                set_last_error(format!("未知 device: {other}（可选 cpu/gpu/vulkan/vulkan:N/cuda）"));
                return ptr::null_mut();
            }
        };

        let mut b: EngineBuilder = Engine::builder().tier(tier).device(device);
        if let Some(p) = preset {
            b = b.preset(p);
        }
        if threads > 0 {
            b = b.threads(threads as usize);
        }
        if detect_orientation >= 0 {
            b = b.detect_orientation(detect_orientation != 0);
        }
        if verify_sha256 == 0 {
            b = b.verify_sha256(false);
        }

        match b.build(dir) {
            Ok(e) => Box::into_raw(Box::new(e)),
            Err(e) => {
                set_last_error(e.to_string());
                ptr::null_mut()
            }
        }
    }));
    match r {
        Ok(p) => p,
        Err(_) => {
            set_last_error("qppocr_create 内部 panic");
            ptr::null_mut()
        }
    }
}

/// 销毁引擎实例（NULL 安全）。
#[no_mangle]
pub unsafe extern "C" fn qppocr_destroy(engine: *mut Engine) {
    if !engine.is_null() {
        // SAFETY: 指针来自 qppocr_create 的 Box::into_raw，且仅在此回收一次。
        let _ = catch_unwind(AssertUnwindSafe(|| drop(unsafe { Box::from_raw(engine) })));
    }
}

fn result_to_json(r: Result<qppocr::OcrResult, qppocr::Error>) -> *mut c_char {
    match r {
        Ok(res) => match serde_json::to_string(&res) {
            Ok(s) => match CString::new(s) {
                Ok(cs) => cs.into_raw(),
                Err(_) => {
                    set_last_error("结果包含内嵌 NUL 字节");
                    ptr::null_mut()
                }
            },
            Err(e) => {
                set_last_error(format!("JSON 序列化失败: {e}"));
                ptr::null_mut()
            }
        },
        Err(e) => {
            set_last_error(e.to_string());
            ptr::null_mut()
        }
    }
}

fn run_guarded(
    engine: *const Engine,
    f: impl FnOnce(&Engine) -> Result<qppocr::OcrResult, qppocr::Error>,
) -> *mut c_char {
    clear_last_error();
    if engine.is_null() {
        set_last_error("engine 句柄为 NULL");
        return ptr::null_mut();
    }
    let r = catch_unwind(AssertUnwindSafe(|| {
        // SAFETY: 调用方保证 engine 是 qppocr_create 返回的有效句柄。
        f(unsafe { &*engine })
    }));
    match r {
        Ok(res) => result_to_json(res),
        Err(_) => {
            set_last_error("OCR 推理内部 panic");
            ptr::null_mut()
        }
    }
}

/// 识别图片文件（PNG/JPEG/BMP），返回 JSON 字符串（需 `qppocr_free_string` 释放）。
/// 失败返回 NULL，错误信息见 `qppocr_last_error`。
#[no_mangle]
pub unsafe extern "C" fn qppocr_run_file(engine: *const Engine, path: *const c_char) -> *mut c_char {
    let Some(p) = (unsafe { cstr_opt(path) }) else {
        set_last_error("path 不能为空或不是有效 UTF-8");
        return ptr::null_mut();
    };
    run_guarded(engine, |e| e.run_image_file(p))
}

/// 识别内存中的图片字节（PNG/JPEG/BMP），返回 JSON 字符串（需 `qppocr_free_string` 释放）。
/// 失败返回 NULL，错误信息见 `qppocr_last_error`。
#[no_mangle]
pub unsafe extern "C" fn qppocr_run_image(
    engine: *const Engine,
    data: *const c_uchar,
    len: usize,
) -> *mut c_char {
    if data.is_null() || len == 0 {
        set_last_error("data 为空");
        return ptr::null_mut();
    }
    // SAFETY: 调用方保证 data 指向 len 字节的有效内存。
    let bytes = unsafe { std::slice::from_raw_parts(data, len) };
    run_guarded(engine, |e| e.run_image_bytes(bytes))
}

/// 释放由 qppocr_run_file / qppocr_run_image 返回的字符串（NULL 安全）。
#[no_mangle]
pub unsafe extern "C" fn qppocr_free_string(s: *mut c_char) {
    if !s.is_null() {
        // SAFETY: 指针来自本库的 CString::into_raw。
        drop(unsafe { CString::from_raw(s) });
    }
}
