package com.internectics.fragment;

import java.io.FileNotFoundException;

import com.internectics.android_flashcardcreator.R;
import com.internectics.helper.FileOperationHelper;
import com.internectics.util.AppContext;
import com.internectics.util.Global;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SimpleAdapter.ViewBinder;

public class CardListBinder implements ViewBinder {  
	  
    @Override  
    public boolean setViewValue(View view, Object data,  
            String textRepresentation) {  
        // TODO Auto-generated method stub  
        if((view instanceof ImageView) && (data instanceof Uri)) {  
            ImageView imageView = (ImageView) view;  
            
            Uri dataUri = (Uri)data;
            Log.d(Global.debugTag, "ViewBinder" + dataUri.toString());
            ContentResolver cResolver = AppContext.getAppContext().getContentResolver();
            Bitmap bitmap;
			try {
				bitmap = BitmapFactory.decodeStream(cResolver
						.openInputStream(dataUri));
				imageView.setImageBitmap(bitmap); 
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
             
            return true;  
        }  
        return false;  
    }  
      
}
