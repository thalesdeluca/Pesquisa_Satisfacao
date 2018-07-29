package com.delucathales.pesquisa_satisfacao;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class HomeActivity extends AppCompatActivity {
    //Eva = Evaluation
    private LinearLayout mClientSignupBtn;
    private LinearLayout mEvaSignupBtn;
    private LinearLayout mResultBtn;
    private TextView mResearchDate;

    private String monthYear;
    private long evCount;
    private DatabaseReference mDatabase;
    private boolean action = false;

    private Calendar actualDate;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        //Inicialização de componentes
        mClientSignupBtn = (LinearLayout)findViewById(R.id.clientSignup_btn);
        mEvaSignupBtn = (LinearLayout)findViewById(R.id.evaluationSignup_btn);
        mResultBtn = (LinearLayout)findViewById(R.id.result_btn);
        mResearchDate = (TextView)findViewById(R.id.date_home_tv);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        actualDate = Calendar.getInstance();
        monthYear = (actualDate.get(Calendar.MONTH) + 1) + "/" + actualDate.get(Calendar.YEAR);

        mResearchDate.setText((actualDate.get(Calendar.MONTH) + 1) + "/" + actualDate.get(Calendar.YEAR));

        mClientSignupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent toSignup= new Intent(v.getContext(), SignUpActivity.class);
                startActivity(toSignup);
            }
        });
        mResultBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent toResult= new Intent(v.getContext(), ResultActivity.class);
                startActivity(toResult);
            }
        });
        mEvaSignupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent toSelect= new Intent(v.getContext(), SelectUserActivity.class);
                startActivity(toSelect);
            }
        });
        if(isOnline() == true) { //Se o dispositivo está conectado, chama o método evaluationCreate
            evaluationCreate();
        }
    }

    private boolean isOnline(){
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

    private void evaluationCreate(){
        //Verifica se a avaliação mensal já está criada e, caso não esteja, cadastra uma nova avaliação, baseando-se
        //na data atual e seleciona 20% dos clientes já cadastrados para participar da mesma
        action = false;
        mDatabase.child("Authorization").child("pesquisa-de-satisfacao-5b24c").child("evaluations").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot data: dataSnapshot.getChildren()){
                    if(data.child("ref").getValue().equals(monthYear) && action == false) {
                        //se a avaliação mensal já está criada e não foi feita nenhuma ação ainda,
                        //para a execução do onDataChange
                        action = true;
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

                            if(customersCount >= 5){ //se a quantidade de clientes é menor que 5, não é possível cadastrar a avaliação mensal

                                mDatabase.child("Authorization").child("pesquisa-de-satisfacao-5b24c").child("evaluations").child(evId).child("ref").setValue(monthYear);
                                //Log.d("Customers MAX::::::", Integer.toString(maxParticipants));
                                int counterSelected = 0;
                                while(counterSelected < maxParticipants){
                                    Random r = new Random();
                                    int customerSelected = r.nextInt(customersCount - 1);
                                    String customerId = "-id" + customerSelected;
                                    if(canBeEvaluated(dataSnapshot.child(customerId).getValue(Customer.class).getLastEvaluationDate())){
                                        //verifica se o cliente pode ser avaliado, baseando-se na data de sua ultima participação
                                        //em avaliações mensais
                                        mDatabase.child("Authorization").child("pesquisa-de-satisfacao-5b24c").child("evaluations")
                                                .child(evId).child("participants").child(customerId).child("flag").setValue("-");

                                        counterSelected++;
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
