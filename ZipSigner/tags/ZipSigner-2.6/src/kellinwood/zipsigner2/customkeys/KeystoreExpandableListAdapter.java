package kellinwood.zipsigner2.customkeys;

import java.util.List;

import kellinwood.zipsigner2.R;


import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

public class KeystoreExpandableListAdapter  extends BaseExpandableListAdapter 
{
    ManageKeysActivity context;
    List<Keystore> keystores;
    
    public KeystoreExpandableListAdapter( ManageKeysActivity context, List<Keystore> keystores)
    {
        this.context = context;
        this.keystores = keystores;
    }

    public void dataChanged(List<Keystore> keystores) {
        this.keystores = keystores;
        notifyDataSetChanged();
    }
    
    public Alias getChild(int groupPosition, int childPosition) {
        return keystores.get(groupPosition).getAliases().get( childPosition);
    }

    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    public int getChildrenCount(int groupPosition) {
        return keystores.get(groupPosition).getAliases().size();
    }

    public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
            View convertView, ViewGroup parent) 
    {
        
        final Alias childAlias = getChild(groupPosition, childPosition);
        
        // Layout parameters for the ExpandableListView
        AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
                ViewGroup.LayoutParams.FILL_PARENT, 64);

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setLayoutParams(lp);
        
        LinearLayout.LayoutParams cblp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        cblp.setMargins(0, 10, 0, 0);
        
        CheckBox checkBox = new CheckBox(context);
        checkBox.setChecked( childAlias.isSelected());
        checkBox.setOnClickListener( new OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox cb = (CheckBox)v;
                childAlias.setSelected(cb.isChecked());
                context.customKeysDataSource.updateAlias(childAlias);
            }
        });
        
        checkBox.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
        checkBox.setLayoutParams(cblp);
        linearLayout.addView(checkBox);
        
        TextView textView = new TextView(context);
        textView.setGravity(Gravity.TOP | Gravity.LEFT);
        textView.setPadding(10, 0, 0, 0);
        
        String text = childAlias.getDisplayName();
        if (!childAlias.getName().equals(childAlias.getDisplayName())) {
            text = text + " (" + childAlias.getName() + ")";
        }
        text = text + "\n" + (childAlias.rememberPassword() ?
                context.getResources().getString(R.string.PasswordIsRemembered) : 
                    context.getResources().getString(R.string.PasswordIsNotRemembered));        
        textView.setText( text);

        linearLayout.setLongClickable(true); // enables access to the context menu
        linearLayout.addView(textView);
        return linearLayout;
    }

    public Keystore getGroup(int groupPosition) {
        return keystores.get( groupPosition);
    }

    public int getGroupCount() {
        return keystores.size();
    }

    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
            ViewGroup parent) 
    {
        
        final Keystore keystore = getGroup( groupPosition);
        
        // Layout parameters for the ExpandableListView
        AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
                ViewGroup.LayoutParams.FILL_PARENT, 64);

        TextView textView = new TextView(context);
        textView.setLayoutParams(lp);
        // Center the text vertically
        textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
        // Set the text starting position
        textView.setPadding(36, 0, 0, 0);
        textView.setText(keystore.getPath()
                + "\n" + (keystore.rememberPassword() ? 
                        context.getResources().getString(R.string.PasswordIsRemembered) : 
                        context.getResources().getString(R.string.PasswordIsNotRemembered)));



        return textView;
    }

    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public boolean hasStableIds() {
        return true;
    }

    
}
