package com.delucathales.pesquisa_satisfacao;

import android.app.AlertDialog;
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
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class EvaluationActivity extends AppCompatActivity {
    //TODO: Apply flag not touchable
    private EditText mReason;
    private RatingBar mRating;
    private TextView mRatingNumber;
    private TextView mEmote;
    private LinearLayout mSendBtn;
    private Dialog okDialog;

    private DatabaseReference mDatabase;

    private Customer customer;
    private String customerId;
    private int customerState;
    private String monthYear;
    private long evaluationsCount;
    private boolean action = false;

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_evaluation);
        getExtras();
        mReason = (EditText) findViewById(R.id.reason_et);
        mRating = (RatingBar) findViewById(R.id.rating_rb);
        mRatingNumber = (TextView) findViewById(R.id.number_rating_tv);
        mEmote = (TextView) findViewById(R.id.emote_tv);
        mSendBtn = (LinearLayout) findViewById(R.id.send_btn);
        okDialog = new Dialog(this);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        context = this;

        mRating.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                mRatingNumber.setText(Float.toString(rating * 2));
                changeEmote(rating * 2);
            }
        });

        mReason.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(count > 0) {
                    if(mSendBtn.getVisibility() == View.GONE)
                        mSendBtn.setVisibility(View.VISIBLE);
                }
                else
                {
                    if(mSendBtn.getVisibility() == View.VISIBLE)
                    mSendBtn.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(v.getContext());
                alertDialog.setTitle("Cadastro de avaliação");
                alertDialog.setMessage("Você tem certeza que quer cadastrar esta avaliação?");

                alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(isOnline() == true) {
                            dialog.dismiss();
                            try {
                                CustomerState state = new CustomerState();
                                int evaluation = (int) (mRating.getRating() * 2);
                                customer.setLastEvaluation(monthYear);
                                customer.setFlag(state.getFlag(evaluation));
                                writeEvaluation(customerId, customer.getFlag(), mReason.getText().toString());
                                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                            }
                            catch (Exception ex) {
                                Toast.makeText(v.getContext(), "Erro!Tente novamente mais tarde", Toast.LENGTH_SHORT).show();
                            }
                            finally {
                                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                okDialog.setContentView(R.layout.dialog_ok);
                                okDialog.setCancelable(false);
                                okDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                    @Override
                                    public void onDismiss(DialogInterface dialog) {
                                        Intent toSignup = new Intent(v.getContext(), HomeActivity.class);
                                        toSignup.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(toSignup);
                                    }
                                });
                                LinearLayout okBtn = (LinearLayout) okDialog.findViewById(R.id.ok_btn);

                                //Fecha Dialog automaticamente após 2 segundos
                                Runnable closeDialog = new Runnable() {
                                    @Override
                                    public void run() {
                                        okDialog.dismiss();
                                        finish();
                                    }
                                };
                                Handler delay = new Handler();
                                delay.postDelayed(closeDialog, 3000);


                                okBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        okDialog.dismiss();
                                        finish();
                                    }
                                });
                                okDialog.show();
                            }
                        }
                        else{
                            okDialog.setContentView(R.layout.dialog_retry);
                            LinearLayout retryBtn = okDialog.findViewById(R.id.tryagain_btn);
                            LinearLayout cancel = okDialog.findViewById(R.id.dismiss_tryagain_btn);

                            retryBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    okDialog.dismiss();
                                    mSendBtn.callOnClick();
                                }
                            });

                            cancel.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    okDialog.dismiss();
                                }
                            });
                            okDialog.show();
                        }

                    }
                });
                alertDialog.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Toast.makeText(v.getContext(),"Operação Cancelada", Toast.LENGTH_SHORT).show();
                    }
                });
                alertDialog.show();
            }
        });
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

    private void getExtras(){
        //Pega informações do cliente passada pela Activity SelectUsers
        Intent intent = getIntent();
        customerState = intent.getIntExtra("customerState", 0);
        customer = intent.getParcelableExtra("customer");
        customerId = customer.getId();
        monthYear = intent.getStringExtra("actualDate");
    }

    @Override
    public void onBackPressed(){
        //Caso aperte para voltar, é mostrado um Dialog solicitando a confirmação da ação do usuário
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Você tem certeza que quer voltar?");
        alertDialog.setMessage("Você PERDERÁ informações de avaliação.");
        alertDialog.setPositiveButton("SIM", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent toSignup = new Intent(context, HomeActivity.class);
                toSignup.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(toSignup);
            }
        });
        alertDialog.setNegativeButton("NÃO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Toast.makeText(alertDialog.getContext() ,"Operação Cancelada", Toast.LENGTH_SHORT).show();
            }
        });
        alertDialog.show();
    }

    private void changeEmote(float rating) {
        //Baseando-se na nota do cliente, é mostrado um Emote diferente
        if(rating > 8) {
            mEmote.setText(getResources().getString(R.string.happy));
        }
        else if(rating > 6) {
            mEmote.setText(getResources().getString(R.string.normal));
        }
        else{
            mEmote.setText(getResources().getString(R.string.disapproval));
        }

    }


    private void writeEvaluation(final String custId, final String flag, final String reason){
        //Cadastra a avaliação do cliente no banco de dados
        DatabaseReference evRef = mDatabase.child("Authorization").child("pesquisa-de-satisfacao-5b24c").child("evaluations");

        evRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot data: dataSnapshot.getChildren()) {
                    if(data.child("ref").getValue().equals(monthYear) && action == false) {
                        String evId = data.getKey();
                        action = true;
                        if (data.child("participants").child(custId).child("flag").getValue().equals("-")) {
                            mDatabase.child("Authorization").child("pesquisa-de-satisfacao-5b24c").child("evaluations").child(evId).child("participants").child(custId).child("flag").setValue(flag);
                            mDatabase.child("Authorization").child("pesquisa-de-satisfacao-5b24c").child("evaluations").child(evId).child("participants").child(custId).child("reason").setValue(reason);
                            mDatabase.child("Authorization").child("pesquisa-de-satisfacao-5b24c").child("customers").child(customer.getId()).child("lastEvaluationDate").setValue(monthYear);
                            mDatabase.child("Authorization").child("pesquisa-de-satisfacao-5b24c").child("customers").child(customer.getId()).child("flag").setValue(flag);

                        }
                    }
                }
                evaluationsCount = dataSnapshot.getChildrenCount();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
