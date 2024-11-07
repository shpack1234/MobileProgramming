package com.ref.project.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ParsingJsonAdapter {
    private void ParsingJson(String json) {
        try {
            JSONObject jsonObject=new JSONObject(json);

            JSONArray refArray=jsonObject.getJSONArray("");

            for(int i=0; i<refArray.length(); i++) {
                JSONObject refObject=refArray.getJSONObject(i);

                RefData refData=new RefData();

                refData.setItemName(refObject.getString("itemDescription"));
                refData.setItemQuantity(refObject.getInt("itemQuantity"));
                refData.setItemId(refObject.getInt("categoryId"));

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
