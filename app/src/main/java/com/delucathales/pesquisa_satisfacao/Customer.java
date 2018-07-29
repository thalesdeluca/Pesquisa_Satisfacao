package com.delucathales.pesquisa_satisfacao;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Customer implements Parcelable{
    private String name;
    private String contact;
    private String lastEvaluationDate;
    private String dateClient;
    private String flag;
    @Exclude
    private String id;


    public Customer(){

    }

    public Customer(String name, String contact, String dateClient, String lastEvaluation){
        this.name = name;
        this.contact = contact;
        this.dateClient = dateClient;
        this.lastEvaluationDate = lastEvaluation;
        this.flag = "-";

    }
    public Customer(Parcel in){
        this.name = in.readString();
        this.contact = in.readString();
        this.dateClient = in.readString();
        this.lastEvaluationDate = in.readString();
        this.flag = in.readString();
        this.id = in.readString();
    }
    public static final Parcelable.Creator<Customer> CREATOR = new Parcelable.Creator<Customer>(){
        @Override
        public Customer createFromParcel(Parcel source) {
            return new Customer(source);
        }

        @Override
        public Customer[] newArray(int size) {
            return new Customer[0];
        }
    };

    @Override
    public int describeContents() {
        return Parcelable.CONTENTS_FILE_DESCRIPTOR;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags){
        dest.writeString(name);
        dest.writeString(contact);
        dest.writeString(dateClient);
        dest.writeString(lastEvaluationDate);
        dest.writeString(flag);
        dest.writeString(id);
    }
    public String getName(){
        return name;
    }
    public String getContact(){
        return contact;
    }
    public String getDateClient(){
        return dateClient;
    }
    public String getLastEvaluationDate() {
        return lastEvaluationDate;
    }
    public String getFlag() {
        return flag;
    }
    public String getId() {
        return id;
    }

    public void setName(String name){
        this.name = name;
    }
    public void setContact(String contact){
        this.contact = contact;
    }
    public void setDateClient(String date){
        dateClient = date;
    }
    public void setLastEvaluation(String lastEvaluation) {
        this.lastEvaluationDate = lastEvaluation;
    }
    public void setFlag(String flag) {
        this.flag = flag;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String toString(){
        return name + " " + contact + " " + dateClient;
    }




}
