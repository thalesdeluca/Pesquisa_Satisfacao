package com.delucathales.pesquisa_satisfacao;

import android.app.Activity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import java.util.List;

public class CustomerAdapter extends BaseAdapter {

    private Activity activity;
    private final List<Customer> selectedCustomers;

    public CustomerAdapter(List<Customer> customerList, Activity act){
        selectedCustomers = customerList;
        activity = act;
    }

    @Override
    public int getCount() {
        if(selectedCustomers != null) {
            return selectedCustomers.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return selectedCustomers.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    private int getEmote(String flag){
        if(!flag.equals("-")) {
            if (flag.equals(CustomerState.PROMOTER)) {
                return R.string.happy;
            }
            if (flag.equals(CustomerState.NEUTRAL)) {
                return R.string.normal;
            }
            if(flag.equals(CustomerState.DETRACTOR)){
                return R.string.disapproval;
            }
        }
        return R.string.none;
    }
    private int getTextColor(String flag){
        if(!flag.equals("-")) {
            if (flag.equals(CustomerState.PROMOTER)) {
                return ContextCompat.getColor(activity, R.color.green);
            }
            if (flag.equals(CustomerState.NEUTRAL)) {
                return ContextCompat.getColor(activity, R.color.colorPrimary);
            }
            if(flag.equals(CustomerState.DETRACTOR)){
               return ContextCompat.getColor(activity, R.color.red);
            }
        }
        return ContextCompat.getColor(activity, R.color.colorPrimary);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = activity.getLayoutInflater().inflate(R.layout.user_select_item, parent, false);
        Customer customer = selectedCustomers.get(position);

        TextView mEnterprise = view.findViewById(R.id.enterprise_tv);
        TextView mContact = view.findViewById(R.id.contact_tv);
        TextView mDateClient = view.findViewById(R.id.date_client_tv);
        TextView mFlag = view.findViewById(R.id.emote_userselected_tv);

        mEnterprise.setText(customer.getName());
        mEnterprise.setTextColor(getTextColor(customer.getFlag()));
        mContact.setText(customer.getContact());
        mDateClient.setText(customer.getDateClient());
        mFlag.setText(getEmote(customer.getFlag()));
        mFlag.setTextColor(getTextColor(customer.getFlag()));

        return view;
    }

}
