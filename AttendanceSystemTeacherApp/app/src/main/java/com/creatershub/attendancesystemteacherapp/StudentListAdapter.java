package com.creatershub.attendancesystemteacherapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class StudentListAdapter extends ArrayAdapter<Student> {

    private static final String TAG = "StudentListAdapter";

    private Context mContext;
    private int mResource;
    private int lastPosition = -1;

    /**
     * Holds variables in a View
     */
    private static class ViewHolder {
        TextView name;
        TextView roll;
        TextView attendance;
    }

    /**
     * Default constructor for the StudentListAdapter
     * @param context
     * @param resource
     * @param objects
     */
    public StudentListAdapter(Context context, int resource, ArrayList<Student> objects) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //get the persons information
        String name = getItem(position).getName();
        String roll = getItem(position).getRoll();
        String attendance = getItem(position).getAttendance();

        //Create the person object with the information
        Student person = new Student(name,roll,attendance);

        //create the view result for showing the animation
        final View result;

        //ViewHolder object
        ViewHolder holder;


        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(mResource, parent, false);
            holder= new ViewHolder();
            holder.name = convertView.findViewById(R.id.textView);
            holder.roll = convertView.findViewById(R.id.textView2);
            holder.attendance = convertView.findViewById(R.id.textView3);

            result = convertView;

            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
            result = convertView;
        }


        Animation animation = AnimationUtils.loadAnimation(mContext,
                (position > lastPosition) ? R.anim.load_down_anim : R.anim.load_up_anim);
        result.startAnimation(animation);
        lastPosition = position;

        holder.name.setText(person.getName());
        holder.roll.setText(person.getRoll());
        holder.attendance.setText(person.getAttendance());


        return convertView;
    }
}


