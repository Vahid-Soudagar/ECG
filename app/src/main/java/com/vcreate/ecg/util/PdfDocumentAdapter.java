package com.vcreate.ecg.util;

import android.content.Context;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

public class PdfDocumentAdapter extends PrintDocumentAdapter {

    private Context context;
    private String path;
    private PrintCompleteListener printCompleteListener;


    public PdfDocumentAdapter(Context context, String path) {
        this.context = context;
        this.path = path;
    }

    public PdfDocumentAdapter(Context context, String path, PrintCompleteListener printCompleteListener) {
        this.context = context;
        this.path = path;
        this.printCompleteListener = printCompleteListener;
    }

    public interface PrintCompleteListener {
        void onPrintComplete();
    }

    @Override
    public void onLayout(PrintAttributes oldAttributes, PrintAttributes newAttributes, CancellationSignal cancellationSignal, LayoutResultCallback layoutResultallback, Bundle extras) {
        if (cancellationSignal.isCanceled()) {
            layoutResultallback.onLayoutCancelled();
        } else {
            PrintDocumentInfo.Builder builder = new PrintDocumentInfo.Builder("test report");
            builder.setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
                    .build();
            layoutResultallback.onLayoutFinished(builder.build(), !newAttributes.equals(oldAttributes));
        }
    }

    @Override
    public void onWrite(PageRange[] pages, ParcelFileDescriptor destination, CancellationSignal cancellationSignal, WriteResultCallback callback) {
        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            File file = new File(path);
            inputStream = new FileInputStream(file);
            outputStream = new FileOutputStream(destination.getFileDescriptor());

            byte[] buffer = new byte[16384];
            int size;

            while ((size = inputStream.read(buffer)) >= 0 && !cancellationSignal.isCanceled()) {
                outputStream.write(buffer, 0, size);
            }

            if (cancellationSignal.isCanceled()) {
                callback.onWriteCancelled();
            } else {
                callback.onWriteFinished(new PageRange[] {PageRange.ALL_PAGES});
                if (printCompleteListener != null) {
                    printCompleteListener.onPrintComplete();
                }
            }

        } catch (Exception e) {
            callback.onWriteFailed(e.getMessage());
            e.printStackTrace();
            Log.d("fileerror", Arrays.toString(e.getStackTrace()));
        } finally {
            try {
                inputStream.close();
                outputStream.close();
            } catch (IOException e) {
                Log.d("fileerror", Arrays.toString(e.getStackTrace()));
                e.printStackTrace();
            }
        }
    }
}
