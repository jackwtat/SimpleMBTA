package jackwtat.simplembta.views;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.ms.square.android.expandabletextview.ExpandableTextView;

import jackwtat.simplembta.R;
import jackwtat.simplembta.model.ServiceAlert;

public class ServiceAlertItem extends RelativeLayout {
    private View rootView;
    private RelativeLayout clickableBackground;
    private LinearLayout headerLayout;
    private ExpandableTextView expandableTextView;
    private TextView headerTextView;
    private TextView shortBodyTextView;
    private ImageView alertIcon;
    private ImageView advisoryIcon;
    private ImageView externalLinkIcon;
    private View bottomBorder;

    public ServiceAlertItem(Context context) {
        super(context);
        init(context);
    }

    public ServiceAlertItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ServiceAlertItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void setServiceAlert(ServiceAlert alert) {
        String headerText = getContext().getResources().getString(
                getContext().getResources().getIdentifier(
                        alert.getEffect(), "string", getContext().getPackageName()));

        StringBuilder headerBuilder;
        if (alert.isActive()) {
            headerBuilder = new StringBuilder(headerText);
        } else {
            headerBuilder = new StringBuilder(getContext().getResources().getString(R.string.UPCOMING))
                    .append(" ").append(headerText);
        }

        String alertHeader = headerBuilder.toString();
        String alertShortBody = alert.getHeader();
        String alertLongBody = alert.getDescription();

        headerTextView.setText(alertHeader);
        shortBodyTextView.setText(alertShortBody);

        if (alertLongBody.length() > 0 && !alertLongBody.equalsIgnoreCase("null")) {
            expandableTextView.setText(alertLongBody);
            expandableTextView.setVisibility(VISIBLE);
        } else {
            expandableTextView.setVisibility(View.GONE);
        }

        if (alert.isUrgent()) {
            alertIcon.setVisibility(View.VISIBLE);
            advisoryIcon.setVisibility(View.GONE);
        } else {
            alertIcon.setVisibility(View.GONE);
            advisoryIcon.setVisibility(View.VISIBLE);
        }

        /*if (alert.getUrl() != null && !alert.getUrl().equals("") &&
                !alert.getUrl().equalsIgnoreCase("null")) {
            externalLinkIcon.setVisibility(View.VISIBLE);

            TypedValue typedValue = new TypedValue();

            getContext().getTheme().resolveAttribute(
                    android.R.attr.selectableItemBackground, typedValue, true);

            if (typedValue.resourceId != 0) {
                clickableBackground.setBackgroundResource(typedValue.resourceId);
            } else {
                clickableBackground.setBackgroundColor(typedValue.data);
            }
        } else {
            externalLinkIcon.setVisibility(View.GONE);

            clickableBackground.setBackgroundColor(
                    ContextCompat.getColor(getContext(), R.color.card_view_background));
        }*/
    }

    public void enableBorder(boolean enable) {
        if (enable) {
            bottomBorder.setVisibility(VISIBLE);
        } else {
            bottomBorder.setVisibility(GONE);
        }
    }

    public void init(Context context) {
        rootView = inflate(context, R.layout.item_service_alert, this);
        clickableBackground = rootView.findViewById(R.id.clickable_background);
        headerLayout = rootView.findViewById(R.id.alert_header);
        expandableTextView = rootView.findViewById(R.id.expand_text_view);
        headerTextView = rootView.findViewById(R.id.alert_header_text_view);
        shortBodyTextView = rootView.findViewById(R.id.alert_short_body_text_view);
        alertIcon = rootView.findViewById(R.id.service_alert_icon);
        advisoryIcon = rootView.findViewById(R.id.service_advisory_icon);
        externalLinkIcon = rootView.findViewById(R.id.external_link_icon);
        bottomBorder = rootView.findViewById(R.id.bottom_border);
    }
}
