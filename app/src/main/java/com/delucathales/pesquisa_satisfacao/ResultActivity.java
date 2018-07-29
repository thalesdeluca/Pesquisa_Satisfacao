package com.delucathales.pesquisa_satisfacao;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Calendar;

//TODO: implement Result
public class ResultActivity extends AppCompatActivity {

    private ListView mCustomersListView;
    private Calendar actualDate;
    private boolean generate = false;
    private Dialog mDialog;

    private DatabaseReference mDatabase;
    private Activity activity = this;

    private TextView mResultTxt;
    private TextView textView;
    private ProgressBar mProgressBar;
    private Button mGenerateResult;

    private ImageButton mRaiseMonthBtn;
    private ImageButton mLowerMonthBtn;
    private ImageButton mRaiseYearBtn;
    private ImageButton mLowerYearBtn;
    private ProgressBar loading;
    private RelativeLayout root;

    private LinearLayout mContent;
    private TextView month;
    private TextView year;

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        context = this;
        mDialog = new Dialog(this);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        actualDate = Calendar.getInstance();

        mProgressBar = findViewById(R.id.progressbar_pb);
        mResultTxt = findViewById(R.id.result_txt);
        mGenerateResult = findViewById(R.id.generate_result_btn);

        mRaiseMonthBtn = findViewById(R.id.raise_month_btn);
        mLowerMonthBtn = findViewById(R.id.lower_month_btn);
        mRaiseYearBtn = findViewById(R.id.raise_year_btn);
        mLowerYearBtn = findViewById(R.id.lower_year_btn);

        mContent = findViewById(R.id.result_content);
        mContent.setVisibility(View.GONE);
        month = findViewById(R.id.month_et);
        year = findViewById(R.id.year_et);

        root = findViewById(R.id.root_results);
        loading = new ProgressBar(this, null, android.R.attr.progressBarStyle);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(200,200);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        root.addView(loading, params);
        loading.setVisibility(View.GONE);

        textView = new TextView(activity, null);
        textView.setText("Não foi realizada uma pesquisa neste período.");
        textView.setTextSize(20);
        textView.setTextColor(ContextCompat.getColor(this, R.color.white));
        RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        params1.addRule(RelativeLayout.CENTER_IN_PARENT);
        root.addView(textView, params1);
        textView.setVisibility(View.GONE);

        mGenerateResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generate = true;
                if(isOnline() == true) {
                    getEvaluation();
                }
                else{
                    mDialog.setContentView(R.layout.dialog_retry);
                    LinearLayout retryBtn = mDialog.findViewById(R.id.tryagain_btn);
                    LinearLayout cancel = mDialog.findViewById(R.id.dismiss_tryagain_btn);

                    retryBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mDialog.dismiss();
                            mGenerateResult.callOnClick();
                        }
                    });

                    cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mDialog.dismiss();
                        }
                    });
                    mDialog.show();
                }
                generate = false;
            }
        });
        mGenerateResult.setVisibility(View.VISIBLE);


        mRaiseMonthBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int monthSelected = Integer.parseInt(month.getText().toString());
                monthSelected = validateMonth(monthSelected);
                int yearSelected = Integer.parseInt(year.getText().toString());
                yearSelected = validateYear(yearSelected);
                if(yearSelected < actualDate.get(Calendar.YEAR)) {
                    if (monthSelected < 12) {
                        monthSelected++;
                    }
                }
                else{
                    if (monthSelected < actualDate.get(Calendar.MONTH) + 1) {
                        monthSelected++;
                    }
                }
                month.setText(Integer.toString(monthSelected));
                getEvaluation();
            }
        });
        mLowerMonthBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int monthSelected = Integer.parseInt(month.getText().toString());
                monthSelected = validateMonth(monthSelected);
                if(monthSelected > 1){
                    monthSelected--;
                }
                month.setText(Integer.toString(monthSelected));
                getEvaluation();
            }
        });
        mRaiseYearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int yearSelected = Integer.parseInt(year.getText().toString());
                yearSelected = validateYear(yearSelected);
                if(yearSelected < actualDate.get(Calendar.YEAR)){
                    yearSelected++;
                }
                if(yearSelected == actualDate.get(Calendar.YEAR)){
                    month.setText(Integer.toString(actualDate.get(Calendar.MONTH) + 1));
                }
                year.setText(Integer.toString(yearSelected));
                getEvaluation();
            }
        });
        mLowerYearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int yearSelected = Integer.parseInt(year.getText().toString());
                yearSelected = validateYear(yearSelected);
                if(yearSelected > 1900){
                    yearSelected--;
                }
                year.setText(Integer.toString(yearSelected));
                getEvaluation();
            }
        });
        month.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                getEvaluation();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        year.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                getEvaluation();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        if(isOnline() == true) {
            getEvaluation();
        }
        else{
            mDialog.setContentView(R.layout.dialog_retry);
            LinearLayout retryBtn = mDialog.findViewById(R.id.tryagain_btn);
            LinearLayout cancel = mDialog.findViewById(R.id.dismiss_tryagain_btn);

            retryBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDialog.dismiss();
                    mGenerateResult.callOnClick();
                }
            });

            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDialog.dismiss();
                }
            });
            mDialog.show();
        }

    }

    private int validateMonth(int monthSelected){
        //Verifica se o mês informado está dentro do range entre o 1 e o mês atual
        if(monthSelected > actualDate.get(Calendar.MONTH + 1)) {
            return actualDate.get(Calendar.MONTH + 1);
        }
        else if(monthSelected < 1){
            return 1;
        }
        return monthSelected;
    }
    private int validateYear(int yearSelected){
    //Verifica se o ano informado está dentro do range entre o 1 e o ano atual
        if(yearSelected > actualDate.get(Calendar.YEAR)) {
            return actualDate.get(Calendar.YEAR);
        }
        else if(yearSelected < 1900){
            return 1900;
        }
        return yearSelected;
    }

    private boolean isOnline(){
        //Verifica se o dispositivo está conectado à internet e retorna a resposta
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        try {
            if (networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected()) {
                return true;
            }
        }
        catch (Exception ex){

        }
        return false;
    }

    private String getMonthYear(String month, String year){
        //retorna a string no formato MM/yyyy
        return month + "/" + year;
    }

    private void writeResult(final String evId){
        //Realiza o cálculo do NPS e armazena sob a avaliação mensal evId
        //após armazenado o valor NPS, é alterada as cores dos itens  dos clientes de acordo com a sua nota
        mDatabase.child("Authorization").child("pesquisa-de-satisfacao-5b24c")
                .child("evaluations").child(evId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int countPromoter = 0;
                int countNeutral = 0;
                int countDetractor = 0;

                for(DataSnapshot data: dataSnapshot.child("participants").getChildren()){
                    if(data.child("flag").getValue().equals(CustomerState.PROMOTER)){
                        countPromoter++;
                    }
                    if(data.child("flag").getValue().equals(CustomerState.NEUTRAL)){
                        countNeutral++;
                    }
                    if(data.child("flag").getValue().equals(CustomerState.DETRACTOR)){
                        countDetractor++;
                    }
                }
                float totalParticipants = (float)(countDetractor + countNeutral + countPromoter);
                float resultNPS = (((countPromoter - countDetractor)) / totalParticipants) * 100;
                String nps = String.format("%.2f", resultNPS);

                try{
                    mDatabase.child("Authorization").child("pesquisa-de-satisfacao-5b24c")
                            .child("evaluations").child(evId).child("nps").setValue(nps);
                }
                catch (Exception ex){

                }
                finally {
                    mResultTxt.setText(nps);
                    nps = nps.replace(",", ".");
                    float result = Float.parseFloat(nps);
                    if(result >= 60){
                        mProgressBar.setProgressDrawable(ContextCompat.getDrawable(context, R.drawable.progressbar_green));
                        mResultTxt.setTextColor(ContextCompat.getColor(context,R.color.green));
                    }
                    else if(result >= 20){
                        mProgressBar.setProgressDrawable(ContextCompat.getDrawable(context, R.drawable.progressbar_yellow));
                        mResultTxt.setTextColor(ContextCompat.getColor(context,R.color.colorPrimary));
                    }
                    else {
                        mProgressBar.setProgressDrawable(ContextCompat.getDrawable(context, R.drawable.progressbar_red));
                        mResultTxt.setTextColor(ContextCompat.getColor(context,R.color.red));
                    }
                    mProgressBar.setSecondaryProgress((int)result + 100);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getEvaluation(){
        //Procura no banco de dados a avaliação mensal,e caso encontre, manda buscar o id dos clientes participantes
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        textView.setVisibility(View.GONE);
        loading.setVisibility(View.VISIBLE);
        mGenerateResult.setVisibility(View.GONE);
        mContent.setVisibility(View.GONE);
        mDatabase.child("Authorization").child("pesquisa-de-satisfacao-5b24c")
                .child("evaluations").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String monthYear = getMonthYear(month.getText().toString(), year.getText().toString());
                for(DataSnapshot data : dataSnapshot.getChildren()){
                    if(data.child("ref").getValue().equals(monthYear)){
                        getSelectedCustomersId(monthYear);
                        return;
                    }
                }
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                textView.setVisibility(View.VISIBLE);
                loading.setVisibility(View.GONE);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void searchForUsers(final ArrayList<String> mCustomerIdList) {
        //beaseando-se na lista de ID's dos clientes participantes, realiza-se a busca das informações destes, e com essas informaçoes
        //é populada uma ListView e colocada a mostra
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        final ArrayList<Customer> mCustomersList = new ArrayList<Customer>();
        mDatabase.child("Authorization").child("pesquisa-de-satisfacao-5b24c")
                .child("customers").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot data: dataSnapshot.getChildren()){
                    if(mCustomerIdList.contains(data.getKey()) && !mCustomerIdList.isEmpty()){
                        Customer customer = new Customer();
                        customer.setContact(data.getValue(Customer.class).getContact());
                        customer.setDateClient(data.getValue(Customer.class).getDateClient());
                        customer.setName(data.getValue(Customer.class).getName());
                        customer.setLastEvaluation(data.getValue(Customer.class).getLastEvaluationDate());
                        customer.setFlag(data.getValue(Customer.class).getFlag());
                        customer.setId(data.getKey());


                        mCustomersList.add(customer);
                        mCustomersListView = findViewById(R.id.participants_lv);
                        CustomerAdapter adapter = new CustomerAdapter(mCustomersList, activity);
                        mCustomersListView.setAdapter(adapter);
                        mGenerateResult.setVisibility(View.GONE);
                        loading.setVisibility(View.GONE);
                        mContent.setVisibility(View.VISIBLE);
                        textView.setVisibility(View.GONE);

                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getNPS(DataSnapshot data){
        //Caso o NPS já tenha sido calculado, é chamado esse método para a alteração do valores
        // de alguns componentes do layout de acordo com o NPS
        String npsComma =  data.child("nps").getValue().toString();
        mResultTxt.setText(npsComma);
        npsComma = npsComma.replace(",",".");
        float nps = Float.parseFloat(npsComma) ;
        if(nps >= 60){
            mProgressBar.setProgressDrawable(ContextCompat.getDrawable(this, R.drawable.progressbar_green));
            mResultTxt.setTextColor(ContextCompat.getColor(this,R.color.green));
        }
        else if(nps >= 20){
            mProgressBar.setProgressDrawable(ContextCompat.getDrawable(this, R.drawable.progressbar_yellow));
            mResultTxt.setTextColor(ContextCompat.getColor(this,R.color.colorPrimary));
        }
        else {
            mProgressBar.setProgressDrawable(ContextCompat.getDrawable(this, R.drawable.progressbar_red));
            mResultTxt.setTextColor(ContextCompat.getColor(this,R.color.red));
        }
        mProgressBar.setSecondaryProgress((int)nps + 100);
    }

    private void getSelectedCustomersId(final String monthYear){
        //São buscados guardados os IDs dos clientes participantes em uma lista;
        //Se o NPS ainda não foi calculado, chama o método writeResult
        //Se o NPS foi calculado, chama o método getNPS
        //Mostra um AlertDialog avisando caso não seja possível gerar o resultado
        //chama o método searchForUsers para popular a ListView
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        final ArrayList<String> mCustomerIdList = new ArrayList<String>();
        mDatabase.child("Authorization").child("pesquisa-de-satisfacao-5b24c")
                .child("evaluations").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                int countEvaluated = 0;
                int totalParticipants = 0;
                for (DataSnapshot data : dataSnapshot.getChildren()) {

                    if (data.child("ref").getValue().equals(monthYear)) {
                        for(DataSnapshot participants: data.child("participants").getChildren()){
                            if(!(participants.child("flag").getValue().equals("-"))){
                                mCustomerIdList.add(participants.getKey());
                                countEvaluated++;
                            }
                            totalParticipants = (int) data.child("participants").getChildrenCount();
                        }
                        if(countEvaluated == totalParticipants){
                            if(!data.hasChild("nps")){
                                writeResult(data.getKey());
                            }else{
                                getNPS(data);
                            }
                            searchForUsers(mCustomerIdList);
                            return;
                        }else{
                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);
                            alertDialog.setTitle("Não foi possível gerar  o resultado");
                            alertDialog.setMessage("Existem clientes participantes que ainda não foram avaliados.");
                            alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    loading.setVisibility(View.GONE);
                                    mContent.setVisibility(View.GONE);
                                    textView.setVisibility(View.GONE);
                                    mGenerateResult.setVisibility(View.VISIBLE);
                                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                    return;
                                }
                            });
                            alertDialog.show();
                        }


                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


}
