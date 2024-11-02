package com.reactnativenexgo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.facebook.react.bridge.*
import com.nexgo.oaf.apiv3.APIProxy
import com.nexgo.oaf.apiv3.DeviceEngine
import com.nexgo.oaf.apiv3.SdkResult
import com.nexgo.oaf.apiv3.device.printer.AlignEnum
import com.nexgo.oaf.apiv3.device.printer.BarcodeFormatEnum
import com.nexgo.oaf.apiv3.device.printer.OnPrintListener
import com.nexgo.oaf.apiv3.device.printer.Printer
import com.nexgo.oaf.apiv3.device.printer.DotMatrixFontEnum;
import com.nexgo.oaf.apiv3.device.printer.FontEntity;
import com.nexgo.oaf.apiv3.device.printer.LineOptionEntity;
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class NexgoModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext),
  OnPrintListener {
  private val TAG = "PrinterSample"
  private var deviceEngine: DeviceEngine? = null
  private var printer: Printer? = null

  private val fontXS = FontEntity(DotMatrixFontEnum.ASC_SONG_6X12)
  private val fontS = FontEntity(DotMatrixFontEnum.ASC_SONG_8X16)
  private val fontM = FontEntity(DotMatrixFontEnum.ASC_SONG_12X24)
  private val fontL = FontEntity(DotMatrixFontEnum.ASC_SONG_BOLD_16X24)
  private val fontXL = FontEntity(DotMatrixFontEnum.ASC_SONG_BOLD_16X32)

  private val lineOption = LineOptionEntity.Builder().setUnderline(false).setMarginLeft(0).setZoomX(false).setZoomY(false).setBold(false).build()

  override fun getName(): String {
    return "Nexgo"
  }

  @RequiresApi(Build.VERSION_CODES.O)
  @ReactMethod
  fun init(
    promise: Promise) {
    try {
      //Initialize the SDK components
      deviceEngine = APIProxy.getDeviceEngine(reactApplicationContext)
      printer = deviceEngine?.printer

      //Initialize the printer
      printer?.initPrinter()

      promise.resolve(true);
    } catch (e : Exception){
      promise.reject("PRINTER_INIT_ERROR", e)
    }
  }

  @RequiresApi(Build.VERSION_CODES.O)
  @ReactMethod
  fun printText(
    text: String,
    fontSize: Int,
    alignment: Int,
    isBold: Boolean,
    promise: Promise
  ) {
    printer?.appendPrnStr(text, this.getFont(fontSize), this.getAlignment(alignment), lineOption)
    promise.resolve(true)
  }

  @RequiresApi(Build.VERSION_CODES.O)
  @ReactMethod
  fun printSpacedAround(
    textLeft: String,
    textRight: String,
    fontSize: Int,
    promise: Promise
  ) {
    printer?.appendPrnStr(textLeft, textRight, this.getFont(fontSize))
    promise.resolve(true)
  }

  @RequiresApi(Build.VERSION_CODES.O)
  @ReactMethod
  fun printQR(
    text: String,
    size: Int,
    moduleSize: Int,
    promise: Promise
  ) {
    try {
      printer?.appendQRcode(
          text,
          size,
          moduleSize,
          3,
          AlignEnum.CENTER
      )
      promise.resolve(true)
    } catch (e: Exception) {
      promise.reject("PRINT_QR_ERROR", e)
    }
  }

  @RequiresApi(Build.VERSION_CODES.O)
  @ReactMethod
  fun execute(promise: Promise) {
    try {
        printer?.startPrint(true, this)
    } catch (e: Exception) {
        promise.reject("PRINT_EXECUTION_ERROR", e)
    }
  }

  private fun getAlignment(value: Int): AlignEnum {
    return when (value) {
      0 -> AlignEnum.LEFT
      1 -> AlignEnum.CENTER
      2 -> AlignEnum.RIGHT
      else -> AlignEnum.LEFT
    }
  }

  private fun getFont(value: Int): FontEntity {
    return when (value) {
      12 -> this.fontXS
      16 -> this.fontS
      24 -> this.fontM
      28 -> this.fontL
      32 -> this.fontXL
      else -> this.fontM
    }
  }

  @Override
  override fun onPrintResult(resultCode: Int) {
    when (resultCode) {
      SdkResult.Success -> Log.d(TAG, "Printer job finished successfully!")
      SdkResult.Printer_Print_Fail -> Log.e(TAG, "Printer Failed: $resultCode")
      SdkResult.Printer_Busy -> Log.e(TAG, "Printer is Busy: $resultCode")
      SdkResult.Printer_PaperLack -> Log.e(TAG, "Printer is out of paper: $resultCode")
      SdkResult.Printer_Fault -> Log.e(TAG, "Printer fault: $resultCode")
      SdkResult.Printer_TooHot -> Log.e(TAG, "Printer temperature is too hot: $resultCode")
      SdkResult.Printer_UnFinished -> Log.w(TAG, "Printer job is unfinished: $resultCode")
      SdkResult.Printer_Other_Error -> Log.e(TAG, "Printer Other_Error: $resultCode")
      else -> Log.e(TAG, "Generic Fail Error: $resultCode")
    }
  }
}
