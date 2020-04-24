
package com.hopding.pdflib;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.pdf.PdfRenderer;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.facebook.react.bridge.NoSuchKeyException;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.hopding.pdflib.factories.PDDocumentFactory;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDPage;
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream;
import com.tom_roush.pdfbox.pdmodel.font.PDFont;
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font;
import com.tom_roush.pdfbox.pdmodel.font.PDType0Font;
import com.tom_roush.pdfbox.util.PDFBoxResourceLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.UUID;

import com.hopding.pdflib.factories.PDPageFactory;

public class PDFLibModule extends ReactContextBaseJavaModule {

  private final ReactApplicationContext reactContext;

  public PDFLibModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;

    PDFBoxResourceLoader.init(reactContext);
    PDPageFactory.init(reactContext);
  }

  @Override
  public String getName() {
    return "PDFLib";
  }

  @RequiresApi(api = Build.VERSION_CODES.O)
  @ReactMethod
  public void getPageData(String path, Integer randomId, Promise promise) {
      String randomName = UUID.randomUUID().toString();

      WritableArray pdfImages = Arguments.createArray();
      WritableArray carrierFrequencies = Arguments.createArray();
      try {
        PdfRenderer renderer = new PdfRenderer(ParcelFileDescriptor.open(new File(path), ParcelFileDescriptor.MODE_READ_ONLY));
        Bitmap bitmap;
        final int pageCount = renderer.getPageCount();
        for (int i = 0; i < pageCount; i++) {
          PdfRenderer.Page page = renderer.openPage(i);

          Integer pageWidth = page.getWidth();
          Integer pageHeight = page.getHeight();

          int bWidth = pageWidth;
          int bHeight = pageHeight;
          float PageRatio = (float)pageHeight / (float)pageWidth;
          if (pageHeight > 1000) {
            bHeight = 1000;
            bWidth = Math.round(1000 / PageRatio);
          }

          Log.e("PDFEDITOR", "PAGE HEIGHT ==> " + pageHeight + " Page Width => " + pageWidth + " HEIGHT ===> " + bHeight + " Width ===> " + bWidth + " Page Ration ===> " + PageRatio);

          int width = this.getReactApplicationContext().getResources().getDisplayMetrics().densityDpi / 72 * bWidth;
          int height = this.getReactApplicationContext().getResources().getDisplayMetrics().densityDpi / 72 * bHeight;
          bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
          Canvas canvas = new Canvas(bitmap);
          canvas.drawColor(Color.WHITE);

          page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
          // Save the render result to an image
          String imagePath = this.getReactApplicationContext().getFilesDir().getAbsolutePath() + "/pdf_data_" + randomId + "/thumbs/" + i + ".png";
          File renderFile = new File(imagePath);
          FileOutputStream fileOut = new FileOutputStream(renderFile);
          bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOut);

          /* Push image to array */
          WritableMap pageData = Arguments.createMap();
          pageData.putString("thumb", imagePath);
          pageData.putInt("width", pageWidth);
          pageData.putInt("height", pageHeight);
          pdfImages.pushMap(pageData);

          // close the page
          page.close();

          WritableMap payload = Arguments.createMap();
          payload.putInt("totalPages", pageCount);
          payload.putInt("currentPage", i + 1);
          this.reactContext
          .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
          .emit("onPDFPageProcess", payload);
        }
        // close the renderer
        renderer.close();
      } catch (Exception ex) {
        promise.reject(ex);
      }
      promise.resolve(pdfImages);
  }

  @ReactMethod
  public void createPDF(ReadableMap documentActions, Promise promise) {
    try {
      PDDocument document = PDDocumentFactory.create(documentActions);
      promise.resolve(PDDocumentFactory.write(document, documentActions.getString("path")));
    } catch (NoSuchKeyException e) {
      e.printStackTrace();
      promise.reject(e);
    } catch (IOException e) {
      e.printStackTrace();
      promise.reject(e);
    }
  }

  @ReactMethod
  public void modifyPDF(ReadableMap documentActions, Promise promise) {
    try {
      PDDocument document = PDDocumentFactory.modify(documentActions);
      promise.resolve(PDDocumentFactory.write(document, documentActions.getString("path")));
    } catch (NoSuchKeyException e) {
      e.printStackTrace();
      promise.reject(e);
    } catch (IOException e ) {
      e.printStackTrace();
      promise.reject(e);
    }
  }

  @ReactMethod
  public void test(String text, Promise promise) {
    File dir = new File(reactContext.getFilesDir().getPath() + "/pdfs");
    dir.mkdirs();

    String pdfPath = dir + "/test.pdf";

    PDDocument document = new PDDocument();
    PDPage page1 = new PDPage();
    PDPage page2 = new PDPage();
    document.addPage(page2);
    document.addPage(page1);

    PDPageContentStream contentStream1;
    PDPageContentStream contentStream2;
    try {
      contentStream1 = new PDPageContentStream(document, page1);
      contentStream1.addRect(5, 500, 100, 100);
      contentStream1.setNonStrokingColor(0, 255, 125);
      contentStream1.fill();
      contentStream1.close();

      PDFont font = PDType1Font.HELVETICA;
      contentStream2 = new PDPageContentStream(document, page2);
      contentStream2.beginText();
      contentStream2.setNonStrokingColor(15, 38, 192);
      contentStream2.setFont(font, 12);
      contentStream2.newLineAtOffset(100, 700);
      contentStream2.showText(text);
      contentStream2.endText();
      contentStream2.close();

      document.save(pdfPath);
      document.close();

      promise.resolve(pdfPath);
    } catch (IOException e) {
      e.printStackTrace();
      promise.reject(e);
    }
  }

  @ReactMethod
  public void getDocumentsDirectory(Promise promise) {
    promise.resolve(reactContext.getFilesDir().getPath());
  }

  @ReactMethod
  public void unloadAsset(String assetName, String destPath, Promise promise) {
    try {
      InputStream is = reactContext.getAssets().open(assetName);
      byte[] buffer = new byte[is.available()];
      is.read(buffer);
      is.close();

      File destFile = new File(destPath);
      File dirFile = new File(destFile.getParent());
      dirFile.mkdirs();

      FileOutputStream fos = new FileOutputStream(destFile);
      fos.write(buffer);
      fos.close();
      promise.resolve(destPath);
    } catch (IOException e) {
      promise.reject(e);
    }
  }

  @ReactMethod
  public void getAssetPath(String assetName, Promise promise) {
    promise.reject(new Exception(
      "PDFLib.getAssetPath() is only available on iOS. Try PDFLib.unloadAsset()"
    ));
  }

  @ReactMethod
  public void measureText(String text, String fontName, int fontSize, Promise promise) {
    try {
      PDDocument document = new PDDocument();
      PDFont font = PDType0Font.load(document, reactContext.getApplicationContext().getAssets().open("fonts/" + fontName + ".ttf"));
      float width = font.getStringWidth(text) / 1000 * fontSize;
      float height = (font.getFontDescriptor().getCapHeight()) / 1000 * fontSize;
      WritableMap map = Arguments.createMap();
      map.putInt("width", (int)width);
      map.putInt("height", (int)height);
      promise.resolve(map);
    } catch (IOException e) {
      promise.reject(e);
    }
  }

}
