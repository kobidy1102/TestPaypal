package com.example.pc_asus.testbraintree;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.braintreepayments.api.dropin.DropInActivity;
import com.braintreepayments.api.dropin.DropInRequest;
import com.braintreepayments.api.dropin.DropInResult;
import com.braintreepayments.api.interfaces.HttpResponseCallback;
import com.braintreepayments.api.internal.HttpClient;
import com.braintreepayments.api.models.PaymentMethodNonce;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    final String API_GET_TOKEN="http://192.168.1.198:8080/braintree/main.php";
    final String API_CHECKOUT="http://192.168.1.198:8080/braintree/checkout.php";
    String token,amount;
    HashMap<String,String> paramsHash;
    Button btn;
    EditText edt;
   final int REQUEST_CODE =12345;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn=findViewById(R.id.btn);
        edt= findViewById(R.id.edt);

        new GetToken().execute();


        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitPayment();
            }
        });


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode== REQUEST_CODE){

            if(resultCode==RESULT_OK){

                DropInResult result= data.getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
                PaymentMethodNonce nonce= result.getPaymentMethodNonce();
                String strNonce= nonce.getNonce();
                if(!edt.getText().toString().isEmpty()){
                    amount=edt.getText().toString();
                    paramsHash= new HashMap<>();
                    paramsHash.put("amount",amount);
                    paramsHash.put("nonce",strNonce);
                    
                    sendPayment();
                }else{
                    Toast.makeText(this, "Bạn chưa nhập số tiền thanh toán", Toast.LENGTH_SHORT).show();
                }

            }else if(resultCode==RESULT_CANCELED){
                Toast.makeText(this, "Hủy thanh toán", Toast.LENGTH_SHORT).show();
            }else{
                Exception error= (Exception) data.getSerializableExtra(DropInActivity.EXTRA_ERROR);
                Log.e("error",error.getMessage());
            }


        }

        super.onActivityResult(requestCode, resultCode, data);
    }


    private void sendPayment(){

        RequestQueue queue= Volley.newRequestQueue(MainActivity.this);

        StringRequest stringRequest= new StringRequest(Request.Method.POST, API_CHECKOUT, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                if(response.toString().contains("Successful"))
                    Toast.makeText(MainActivity.this, "successful", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(MainActivity.this, "failed", Toast.LENGTH_SHORT).show();

                Log.e("abc","response:"+response.toString());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Log.e("error",error.getMessage());

            }
        })

        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                if(paramsHash==null){
                    return null;
                }
                Map<String,String> params= new HashMap<>();
                for(String key:paramsHash.keySet()){
                    params.put(key,paramsHash.get(key));

                }
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {

                Map<String,String> params= new HashMap<>();
                params.put("Content-Type","application/x-www-form-urlencoded");


                return params;
            }
        };

        queue.add(stringRequest);


    }


    private void submitPayment(){

        DropInRequest dropInRequest= new DropInRequest().clientToken(token);
        startActivityForResult(dropInRequest.getIntent(this),REQUEST_CODE);
    }



    private class GetToken extends AsyncTask{

        @Override
        protected Object doInBackground(Object[] objects) {

            HttpClient client= new HttpClient();
            client.get(API_GET_TOKEN, new HttpResponseCallback() {
                @Override
                public void success(String responseBody) {
                    token= responseBody;
                }

                @Override
                public void failure(Exception exception) {

                    Log.e("error",exception.getMessage());


                }
            });
            return null;
        }
    }
}
