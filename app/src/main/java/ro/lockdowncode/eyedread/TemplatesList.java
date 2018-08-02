package ro.lockdowncode.eyedread;

import android.content.Context;
import android.content.Intent;
import android.os.Message;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.transform.Templates;

import ro.lockdowncode.eyedread.communication.CommunicationService;

public class TemplatesList extends AppCompatActivity implements TreeNode.TreeNodeClickListener {

    private static TemplatesList instance;

    public static TemplatesList getInstance() {
        return instance;
    }

    private AndroidTreeView tView;
    private List<TemplateListItemView.TemplateListItem> selected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_templates_list);

        instance = this;
        selected = new ArrayList<>();

        //request templates from server
        Message msg = new Message();
        Bundle data = new Bundle();
        data.putString("destination", MainActivity.getInstance().getActiveDesktopConnection().getIp());
        data.putString("message", "0015:DocTemplates");
        msg.setData(data);
        CommunicationService.uiMessageReceiverHandler.sendMessage(msg);

    }

    public void validTemplatesReceived(final String jsonContent) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String lines[] = jsonContent.split("#");
                TreeNode root = TreeNode.root();
                try {
                    for (String jsonLine : lines) {
                        JSONObject lineObj = new JSONObject(jsonLine);
                        String folder = lineObj.getString("folderName");
                        JSONArray files = lineObj.getJSONArray("filesNamesInFolder");
                        TreeNode parentItem = new TreeNode(new TemplateListItemView.TemplateListItem(folder)).setViewHolder(new TemplateListItemView(TemplatesList.this));
                        for (int i = 0; i < files.length(); i++) {
                            String template = files.getString(i);
                            TreeNode templateItem = new TreeNode(new TemplateListItemView.TemplateListItem(template, folder)).setViewHolder(new TemplateListItemView(TemplatesList.this));
                            parentItem.addChild(templateItem);
                            System.out.println(template + " - " + folder);
                        }
                        root.addChild(parentItem);
                    }
                } catch (JSONException e) {
                    e.printStackTrace(System.out);
                }

                tView = new AndroidTreeView(TemplatesList.this.getApplicationContext(), root);
                tView.setDefaultNodeClickListener(TemplatesList.this);
                LinearLayout l = findViewById(R.id.templateList);
                l.addView(tView.getView());
                tView.expandAll();
            }
        });
    }

    @Override
    public void onClick(TreeNode node, Object value) {
        TemplateListItemView.TemplateListItem item = (TemplateListItemView.TemplateListItem) value;
        if (!item.isFolder) {
            View nodeView = node.getViewHolder().getView();
            if (!selected.contains(item)) {
                selected.add(item);
                nodeView.setBackgroundColor(getResources().getColor(R.color.colorEyeD));
            } else {
                selected.remove(item);
                nodeView.setBackgroundColor(getResources().getColor(R.color.mdtp_white));
            }
        }
    }

    public void btnClicked(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.btnBack:
                MainActivity.getInstance().cancelCurrentServerProcess();
                Intent homeIntent = new Intent(this, MainActivity.class);
                startActivity(homeIntent);
                break;
            case R.id.btnNext:
                if (selected.isEmpty()) {
                    new AlertDialog.Builder(this)
                            .setTitle("Atentie")
                            .setMessage("Nu ai selectat niciun sablon!").show();
                } else {
                    //send fields
                    Message msg = new Message();
                    Bundle data = new Bundle();
                    data.putString("destination", MainActivity.getInstance().getActiveDesktopConnection().getIp());
                    data.putString("message", "0018:Valid:"+getIntent().getStringExtra("dataJson"));
                    msg.setData(data);
                    CommunicationService.uiMessageReceiverHandler.sendMessage(msg);
                }
                break;
        }
    }

    private String getTemplatesSelectedMessage() {
        HashMap<String, List<String>> folderTemplatesMap = new HashMap<>();
        for (TemplateListItemView.TemplateListItem selection: selected) {
            List<String> tempList;
            if (folderTemplatesMap.containsKey(selection.folderName)) {
                tempList = folderTemplatesMap.get(selection.folderName);
            } else {
                tempList = new ArrayList<>();
            }
            tempList.add(selection.name);
            folderTemplatesMap.put(selection.folderName, tempList);
        }

        String result = "";
        for (String folder: folderTemplatesMap.keySet()) {
            JSONObject folderObj = new JSONObject();
            JSONArray templatesInFolder = new JSONArray();
            for (String template: folderTemplatesMap.get(folder)) {
                templatesInFolder.put(template);
            }
            try {
                folderObj.put("filesNamesInFolder", templatesInFolder);
                folderObj.put("folderName", folder);
                result = result + folderObj.toString() + "#";
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (!result.isEmpty()) {
            return result.substring(0, result.length()-1);
        }
        return "Error";
    }

    public void readyToSendTemplates() {
        //send templates
        Message msg = new Message();
        Bundle data = new Bundle();
        data.putString("destination", MainActivity.getInstance().getActiveDesktopConnection().getIp());
        data.putString("message", "0019:TemplatesList:"+getTemplatesSelectedMessage());
        msg.setData(data);
        CommunicationService.uiMessageReceiverHandler.sendMessage(msg);
    }

    public void requestStatus(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(TemplatesList.this)
                        .setTitle("Info")
                        .setMessage(message).show();
            }
        });

    }
}

class TemplateListItemView extends TreeNode.BaseNodeViewHolder<TemplateListItemView.TemplateListItem> {

    public TemplateListItemView(Context context) {
        super(context);
    }

    @Override
    public View createNodeView(TreeNode node, TemplateListItem item) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        View view;
        if (item.isFolder) {
            view = inflater.inflate(R.layout.template_list_folder, null, false);
        } else {
            view = inflater.inflate(R.layout.template_list_item, null, false);
        }
        TextView tvName = view.findViewById(R.id.template_list_item_name);
        tvName.setText(item.name);
        return view;
    }

    public static class TemplateListItem {

        public String name;
        public boolean isFolder;
        public String folderName;

        public TemplateListItem(String name) {
            this.name = name;
            this.isFolder = true;
            this.folderName = null;
        }

        public TemplateListItem(String name, String folderName) {
            this.name = name;
            this.isFolder = false;
            this.folderName = folderName;
        }
    }
}
