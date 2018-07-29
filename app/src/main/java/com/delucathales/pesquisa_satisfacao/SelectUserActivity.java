package com.delucathales.pesquisa_satisfacao;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class SelectUserActivity extends AppCompatActivity {

    private ListView mCustomersListView;
    private String monthYear;
    private Calendar actualDate;

    private ProgressBar loading;
    private Dialog mDialog;

    private boolean action = false;
    private DatabaseReference mDatabase;
    private long evCount;
    private Activity activity = this;
    private TextView noResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_user);
        //inicialização dos componentes
        noResults = findViewById(R.id.no_results_tv);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        actualDate = Calendar.getInstance();
        mDialog = new Dialog(this);
        monthYear = (actualDate.get(Calendar.MONTH) + 1) + "/" + actualDate.get(Calendar.YEAR);

        RelativeLayout root = findViewById(R.id.root_userselected);
        loading = new ProgressBar(this, null, android.R.attr.progressBarStyle);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(200,200);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        root.addView(loading, params);
        loading.setVisibility(View.VISIBLE);
        noResults.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        noResults.setVisibility(View.GONE);


        final LinearLayout refresh = findViewById(R.id.refresh_btn);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isOnline() == true) {
                    getSelectedCustomersId();
                    loading.setVisibility(View.VISIBLE);
                    noResults.setVisibility(View.GONE);
                }
                else{
                    mDialog.setContentView(R.layout.dialog_retry);
                    LinearLayout retryBtn = mDialog.findViewById(R.id.tryagain_btn);
                    LinearLayout cancel = mDialog.findViewById(R.id.dismiss_tryagain_btn);

                    retryBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mDialog.dismiss();
                            refresh.callOnClick();
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
        });



    }
    @Override
    public void onStart(){
        super.onStart();
        if(isOnline() == true) {//caso esteja online, tenta criar a avaliação mensal no BD
            evaluationCreate();
        }

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
                        mCustomersListView = findViewById(R.id.selected_user_lv);
                        CustomerAdapter adapter = new CustomerAdapter(mCustomersList, activity);
                        mCustomersListView.setAdapter(adapter);
                        loading.setVisibility(View.GONE);
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                        mCustomersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                Intent toEva = new Intent(view.getContext(), EvaluationActivity.class);
                                toEva.putExtra("customer", mCustomersList.get(position));
                                toEva.putExtra("customerState", CustomerState.REGISTERED);
                                toEva.putExtra("actualDate", monthYear);
                                startActivity(toEva);
                            }
                        });
                        adapter.notifyDataSetChanged();
                    }
                }
                if(mCustomerIdList.isEmpty()){
                    try {
                        loading.setVisibility(View.GONE);
                        noResults.setVisibility(View.VISIBLE);

                    }catch (Exception ex){

                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getSelectedCustomersId(){
        //São buscados guardados os IDs dos clientes participantes em uma lista;
        //chama o método searchForUsers para popular a ListView
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        final ArrayList<String> mCustomerIdList = new ArrayList<String>();
        mDatabase.child("Authorization").child("pesquisa-de-satisfacao-5b24c")
                .child("evaluations").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {

                    if (data.child("ref").getValue().equals(monthYear)) {
                        for(DataSnapshot participants: data.child("participants").getChildren()){
                            if(participants.child("flag").getValue().equals("-")){
                                mCustomerIdList.add(participants.getKey());
                            }
                        }
                        searchForUsers(mCustomerIdList);
                        return;

                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void evaluationCreate(){
        //Verifica se a avaliação mensal já está criada e, caso não esteja, cadastra uma nova avaliação, baseando-se
        //na data atual e seleciona 20% dos clientes já cadastrados para participar da mesma
        action = false;
        mDatabase.child("Authorization").child("pesquisa-de-satisfacao-5b24c").child("evaluations").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot data: dataSnapshot.getChildren()){
                    if(data.child("ref").getValue().equals(monthYear) && action == false) {
                        action = true;
                        getSelectedCustomersId();
                        return;
                    }
                }
                evCount = dataSnapshot.getChildrenCount();
                if(action == false){
                    action = true;
                    //Log.d("STATE::::::::", "ADD PARTICIPANTS");
                    final String evId = "-idEv" + evCount;

                    mDatabase.child("Authorization").child("pesquisa-de-satisfacao-5b24c")
                            .child("customers").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            int customersCount = (int) dataSnapshot.getChildrenCount();
                            //Log.d("Customers Count::::::", Integer.toString(customersCount));
                            int maxParticipants = (int)(customersCount * 0.2);
                            if(customersCount >= 5){
                                mDatabase.child("Authorization").child("pesquisa-de-satisfacao-5b24c").child("evaluations").child(evId).child("ref").setValue(monthYear);
                                //Log.d("Customers MAX::::::", Integer.toString(maxParticipants));
                                int counterSelected = 0;
                                while(counterSelected < maxParticipants){
                                    Random r = new Random();
                                    int customerSelected = r.nextInt(customersCount - 1);
                                    String customerId = "-id" + customerSelected;
                                    if(canBeEvaluated(dataSnapshot.child(customerId).getValue(Customer.class).getLastEvaluationDate())){

                                        mDatabase.child("Authorization").child("pesquisa-de-satisfacao-5b24c").child("evaluations")
                                                .child(evId).child("participants").child(customerId).child("flag").setValue("-");

                                        counterSelected++;
                                    }
                                }
                                getSelectedCustomersId();
                            }
                            else{
                                loading.setVisibility(View.GONE);
                                noResults.setVisibility(View.VISIBLE);
                                noResults.setText("Não foi possível selecionar participantes, pois existem menos de 5 clientes cadastrados");
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

        });

    }

    private boolean canBeEvaluated(String lastEvaluation){
        //Se a ultima avaliação do cliente foi há mais de 2 meses, então ele pode participar da avaliação
        if(lastEvaluation.equals("-")){
            return true;
        }
        Calendar actualDate = Calendar.getInstance();
        Calendar lastDate = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
        String now = actualDate.get(Calendar.DAY_OF_MONTH) + "/" + lastEvaluation;
        Date today = actualDate.getTime();
        try {
            Date a = format.parse(now);
            lastDate.setTime(a);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        int monthsApart = 0;
        while(lastDate.getTime().before(today)){
            lastDate.add(Calendar.MONTH, 1);
            monthsApart++;
        }
        if(monthsApart > 2){
            return true;
        }
        else{
            return false;
        }
    }


}
