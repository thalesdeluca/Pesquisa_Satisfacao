package com.delucathales.pesquisa_satisfacao;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class SignUpActivity extends AppCompatActivity {

    private LinearLayout mNextBtn;
    private EditText mDate;
    private EditText mNick;
    private EditText mName;
    private TextView mResearchDate;

    private Dialog mDialog;

    private Context context;

    private String monthYear;

    private Calendar actualDate;
    private SimpleDateFormat dateFormat;

    private DatabaseReference mDatabase;

    DatePickerDialog.OnDateSetListener datePicker;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        //Inicialização de componentes
        mNextBtn = (LinearLayout)findViewById(R.id.next_btn);
        mDate = (EditText)findViewById(R.id.date_pick_et);
        mResearchDate = (TextView)findViewById(R.id.research_date_tv);
        mName = (EditText)findViewById(R.id.name_et);
        mNick = (EditText)findViewById(R.id.nick_et);
        mDialog = new Dialog(this);
        context = this;
        mDatabase = FirebaseDatabase.getInstance().getReference();

        actualDate = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("dd/MM/yyy", Locale.US);
        initializeDatePicker();
        monthYear = (actualDate.get(Calendar.MONTH) + 1) + "/" + actualDate.get(Calendar.YEAR);
        mResearchDate.setText(monthYear);

        mDate.setText(dateFormat.format(actualDate.getTime()));

        mNextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isOnline() == true) {
                    try {
                        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        Customer customer = new Customer(mName.getText().toString(), mNick.getText().toString(), mDate.getText().toString(), "-");
                        checkIfCustomerSignedup(customer);
                    }
                    catch (Exception ex) {

                    }
                }
                else{
                    mDialog.setContentView(R.layout.dialog_retry);
                    LinearLayout retryBtn = mDialog.findViewById(R.id.tryagain_btn);
                    LinearLayout cancel = mDialog.findViewById(R.id.dismiss_tryagain_btn);

                    retryBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mDialog.dismiss();
                            mNextBtn.callOnClick();
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
        mDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog calendar = new DatePickerDialog(v.getContext(), android.R.style.Theme_Holo_Light_Dialog, datePicker,
                        actualDate.get(Calendar.YEAR), actualDate.get(Calendar.MONTH), actualDate.get(Calendar.DAY_OF_MONTH));
                calendar.getWindow().setBackgroundDrawableResource(R.color.transparent);
                calendar.setTitle("Data em que se tornou cliente");
                calendar.show();
            }
        });

        mNick.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateNext(mNextBtn);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateNext(mNextBtn);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void initializeDatePicker() {
        //Inicializa o componente Date Picker e o seta para a data atual
        datePicker = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                actualDate.set(Calendar.YEAR, year);
                actualDate.set(Calendar.MONTH, month);
                actualDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                mDate.setText(dateFormat.format(actualDate.getTime()));
            }
        };
    }

    private boolean validateNext(View btn) {
        //Verifica se todos os campos estão preenchidos e, caso estejam, mostra o botão Prosseguir
            if(mName.length() == 0 || mNick.length() == 0) {
                if(btn.getVisibility() == View.VISIBLE) {
                    btn.setVisibility(View.GONE);
                }
                return false;
            }
            else {
                if(btn.getVisibility() == View.GONE) {
                    btn.setVisibility(View.VISIBLE);
                }
                return true;
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

    private void writeUser(final Customer customer) {
        //Cadastra o usuário no banco de dados
        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference().child("Authorization").child("pesquisa-de-satisfacao-5b24c").child("customers");
        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final long num = dataSnapshot.getChildrenCount();
                String customerId = "-id" + num;
                mDatabase.child("Authorization").child("pesquisa-de-satisfacao-5b24c").child("customers").child(customerId).setValue(customer);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void checkIfCustomerSignedup(final Customer customer){
        //Verifica se o cliente informado já está cadastrado no banco de dados e, caso
        //não esteja, chama o método writeUser passando como parâmetro o cliente
        DatabaseReference ref = mDatabase.child("Authorization").child("pesquisa-de-satisfacao-5b24c").child("customers");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Customer target = new Customer();
                boolean action = false;
                for(DataSnapshot data: dataSnapshot.getChildren()) {

                    target.setName(data.getValue(Customer.class).getName());
                    target.setContact(data.getValue(Customer.class).getContact());
                    target.setDateClient(data.getValue(Customer.class).getDateClient());
                    target.setLastEvaluation(data.getValue(Customer.class).getLastEvaluationDate());

                    if(isCustomerEqual(target, customer) == true){//Se o cliente já se encontra cadastrado no sistema
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                        alertDialog.setTitle("Cliente já cadastrado");
                        alertDialog.setMessage("Este cliente já se encontra cadastrado no sistema.");
                        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                finish();
                            }
                        });
                        alertDialog.show();
                        action = true;
                    }
                }
                if(action == false) {
                    try {
                        writeUser(customer);

                    }catch (Exception ex){

                    }finally {
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        mDialog.setContentView(R.layout.dialog_usersigned);
                        mDialog.setCancelable(false);
                        mDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                Intent toSignup = new Intent(context, SignUpActivity.class);
                                toSignup.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(toSignup);
                            }
                        });
                        LinearLayout okBtn = (LinearLayout) mDialog.findViewById(R.id.back_btn);

                        //Fecha Dialog automaticamente após 2 segundos
                        Runnable closeDialog = new Runnable() {
                            @Override
                            public void run() {
                                mDialog.dismiss();
                                finish();
                            }
                        };
                        Handler delay = new Handler();
                        delay.postDelayed(closeDialog, 3000);


                        okBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mDialog.dismiss();
                                finish();
                            }
                        });
                        mDialog.show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

        });
    }

    private boolean isCustomerEqual(Customer customer1, Customer customer2){
        //Verifica se o cliente 1 é igual o cliente 2 e retorna a resposta
        if(customer1.toString().equals(customer2.toString())) {

            return true;
        }else {
            return false;
        }
    }






}
