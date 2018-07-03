/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ro.lockdowncode.eyedread.ocr;

import android.content.res.Resources;
import android.graphics.Rect;
import android.util.Log;
import android.util.SparseArray;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.Line;
import com.google.android.gms.vision.text.TextBlock;

import ro.lockdowncode.eyedread.R;
import ro.lockdowncode.eyedread.UI.GraphicOverlay;

/**
 * A very simple Processor which gets detected TextBlocks and adds them to the overlay
 * as OcrGraphics.
 */
public class OcrDetectorProcessor implements Detector.Processor<TextBlock> {

    //private GraphicOverlay<OcrGraphic> graphicOverlay;
    private TextView top,bottom;
    private LinearLayout mrzArea;
    private int[] widthHeight;

    private int[] processingBox;

    public OcrDetectorProcessor(LinearLayout mrzArea,int[] widthHeight)
    {
        this.mrzArea = mrzArea;

        top = (TextView)mrzArea.getChildAt(0);
        bottom = (TextView)mrzArea.getChildAt(1);

        processingBox = new int[4];
        this.widthHeight = widthHeight;
    }



    private void setProcessingArea()
    {
        //area is always in index order : left, right, top, bottom
        int[] leftTop = new int[2];

        mrzArea.getLocationOnScreen(leftTop);

        int swidth,sheight,pwidth,pheight;

        swidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        sheight = Resources.getSystem().getDisplayMetrics().heightPixels;
        pwidth = 1280;
        pheight =1024;


        processingBox[0] = (int)((double)leftTop[0]*((double)pwidth/(double)swidth));
        processingBox[1] = (int)((double)(leftTop[0] + widthHeight[0])*((double)pwidth/(double)swidth));
        processingBox[2] =(int)((double)leftTop[1]*((double)pheight/(double)sheight));
        processingBox[3] =(int)((double)(leftTop[1] + widthHeight[1])*((double)pheight/(double)sheight));

        processingBox[0]= (int)((double)processingBox[0]*0.85);
        processingBox[1] = (int)((double)processingBox[1]*1.15);
        processingBox[2] = (int)((double)processingBox[2]*0.85);
        processingBox[3] = (int)((double)processingBox[3]*1.15);
    // scale this shit to preview sizes for fuck's sake
    }

    /**
     * Called by the detector to deliver detection results.
     * If your application called for it, this could be a place to check for
     * equivalent detections by tracking TextBlocks that are similar in location and content from
     * previous frames, or reduce noise by eliminating TextBlocks that have not persisted through
     * multiple detections.
     */
    @Override
    public void receiveDetections(Detector.Detections<TextBlock> detections) {
        //top.setText(R.string.MRZ_hint);
        //bottom.setText(R.string.MRZ_hint);
        if (processingBox[0] == 0)
            setProcessingArea();

        SparseArray<TextBlock> items = detections.getDetectedItems();
        for (int i = 0; i < items.size(); ++i)
        {
            TextBlock item = items.valueAt(i);
            if (item != null && item.getValue() != null)
            {

                Rect box =item.getBoundingBox();
                //Log.d("OcrDetectorProcessor", String.format("Box left right up down %s %s %s %s",box.left,box.right,box.top,box.bottom ));

                // check if it's inside processing box
                if(box.left >= processingBox[0] && box.right<= processingBox[1] && box.top >= processingBox[2] && box.bottom <= processingBox[3])
                {
                    Log.d("OcrDetectorProcessor", "Text detected! " + item.getValue());
                    //Log.d("OcrDetectorProcessor", String.format("Box left right up down %s %s %s %s",box.left,box.right,box.top,box.bottom ));
                    //Log.d("OcrDetectorProcessor", "text in box!");
                    String[] splitted = item.getValue().split("\\n");
                    if(splitted.length == 2)
                    {
                        top.setText(splitted[0]);

                        splitted[1] = splitted[1].replaceAll("\\s","");
                        //bottom.setText(splitted[1]);
                        if(splitted[1].length()==44)
                            if(splitted[1].charAt(44-3) == '<') {
                                Log.d("OcrDetectorProcessor", "bottom line length is" + splitted[1].length());
                                bottom.setText(splitted[1]);
                        }
                    }
                }

            }
        }
    }

    /**
     * Frees the resources associated with this detection processor.
     */
    @Override
    public void release() {

    }
}
