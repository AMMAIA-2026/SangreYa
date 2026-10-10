package com.ammaia_ispc.sangreyamobile.helpers;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.ammaia_ispc.sangreyamobile.R;
import com.ammaia_ispc.sangreyamobile.model.DashboardCampaignStatus;
import com.ammaia_ispc.sangreyamobile.model.DashboardMonthlyDonors;

import java.util.Collections;
import java.util.List;

public class DashboardChartView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private List<DashboardMonthlyDonors> monthlyDonors = Collections.emptyList();
    private List<DashboardCampaignStatus> campaignStatuses = Collections.emptyList();
    private String emptyMessage = "";
    private boolean donutChart;

    public DashboardChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint.setTypeface(Typeface.create("sans", Typeface.NORMAL));
        // BUG-020: TalkBack debe poder enfocar el gráfico y leer su descripción.
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
    }

    public void setMonthlyDonors(List<DashboardMonthlyDonors> monthlyDonors) {
        donutChart = false;
        this.monthlyDonors = monthlyDonors;
        updateAccessibilityDescription();
        invalidate();
    }

    public void setCampaignStatuses(List<DashboardCampaignStatus> campaignStatuses) {
        donutChart = true;
        this.campaignStatuses = campaignStatuses;
        updateAccessibilityDescription();
        invalidate();
    }

    public void setEmptyMessage(String emptyMessage) {
        this.emptyMessage = emptyMessage == null ? "" : emptyMessage;
        updateAccessibilityDescription();
        invalidate();
    }

    // BUG-020: arma el texto que TalkBack lee con los valores del gráfico,
    // porque lo dibujado en el Canvas no es accesible por sí solo.
    private void updateAccessibilityDescription() {
        StringBuilder description = new StringBuilder();
        if (donutChart) {
            int total = AdminDashboardHelper.totalCampaigns(campaignStatuses);
            description.append("Gráfico de campañas por estado. Total: ")
                    .append(total)
                    .append(" campañas.");
            for (DashboardCampaignStatus status : campaignStatuses) {
                description.append(' ')
                        .append(status.status)
                        .append(": ")
                        .append(status.count)
                        .append(", ")
                        .append(AdminDashboardHelper.percentage(status.count, total))
                        .append(" por ciento.");
            }
        } else if (monthlyDonors.isEmpty()) {
            description.append(emptyMessage);
        } else {
            description.append("Gráfico de barras por mes.");
            for (DashboardMonthlyDonors donor : monthlyDonors) {
                description.append(' ')
                        .append(AdminDashboardHelper.monthLabel(donor.month))
                        .append(' ')
                        .append(donor.year)
                        .append(": ")
                        .append(donor.count)
                        .append('.');
            }
        }
        setContentDescription(description.toString());
    }

    // BUG-021: los textos usan "sp" para crecer con el tamaño de fuente del sistema.
    private float sp(float value) {
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                value,
                getResources().getDisplayMetrics());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (donutChart) {
            drawDonut(canvas);
        } else {
            drawBars(canvas);
        }
    }

    private void drawDonut(Canvas canvas) {
        float density = getResources().getDisplayMetrics().density;
        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;
        float radius = Math.min(getWidth(), getHeight()) / 2f - 20 * density;
        int total = AdminDashboardHelper.totalCampaigns(campaignStatuses);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(25 * density);
        RectF bounds = new RectF(
                centerX - radius,
                centerY - radius,
                centerX + radius,
                centerY + radius);

        if (campaignStatuses.isEmpty()) {
            paint.setColor(ContextCompat.getColor(getContext(), R.color.border));
            canvas.drawArc(bounds, 0, 360, false, paint);
        } else {
            float startAngle = -90;
            for (DashboardCampaignStatus status : campaignStatuses) {
                float sweep = total == 0 ? 0 : status.count * 360f / total;
                paint.setColor(ContextCompat.getColor(
                        getContext(),
                        CampaignHelper.statusColor(status.status)));
                canvas.drawArc(bounds, startAngle, sweep, false, paint);
                startAngle += sweep;
            }
        }

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(ContextCompat.getColor(getContext(), R.color.primary_text));
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setTextSize(sp(22));
        canvas.drawText(String.valueOf(total), centerX, centerY + 3 * density, paint);
        paint.setTypeface(Typeface.DEFAULT);
        paint.setTextSize(sp(10));
        canvas.drawText(
                getContext().getString(R.string.dashboard_campaigns_label),
                centerX,
                centerY + 3 * density + sp(16),
                paint);
    }

    private void drawBars(Canvas canvas) {
        if (monthlyDonors.isEmpty()) {
            if (!emptyMessage.isEmpty()) {
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(ContextCompat.getColor(getContext(), R.color.secondary_text));
                paint.setTextAlign(Paint.Align.CENTER);
                paint.setTypeface(Typeface.DEFAULT);
                paint.setTextSize(sp(12));
                canvas.drawText(emptyMessage, getWidth() / 2f, getHeight() / 2f, paint);
            }
            return;
        }

        float density = getResources().getDisplayMetrics().density;
        float labelSize = sp(9);
        // Las etiquetas de mes y año se ubican según su tamaño real,
        // así no se pisan cuando la fuente del sistema es grande.
        float yearBaseline = getHeight() - 5 * density;
        float monthBaseline = yearBaseline - labelSize - 2 * density;
        float left = 14 * density;
        float right = getWidth() - 8 * density;
        float top = labelSize + 15 * density;
        float baseline = monthBaseline - labelSize - 3 * density;
        float chartHeight = baseline - top;
        int max = 0;
        for (DashboardMonthlyDonors donor : monthlyDonors) {
            max = Math.max(max, donor.count);
        }

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(density);
        paint.setColor(ContextCompat.getColor(getContext(), R.color.border));
        canvas.drawLine(left, baseline, right, baseline, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(Typeface.DEFAULT);
        paint.setTextSize(labelSize);

        float slotWidth = (right - left) / monthlyDonors.size();
        float barWidth = Math.min(27 * density, slotWidth * 0.58f);
        for (int index = 0; index < monthlyDonors.size(); index++) {
            DashboardMonthlyDonors donor = monthlyDonors.get(index);
            float height = max == 0 ? 0 : chartHeight * donor.count / max;
            float center = left + slotWidth * index + slotWidth / 2;
            paint.setColor(ContextCompat.getColor(getContext(), R.color.primary_red));
            canvas.drawRoundRect(
                    center - barWidth / 2,
                    baseline - height,
                    center + barWidth / 2,
                    baseline,
                    5 * density,
                    5 * density,
                    paint);

            paint.setColor(ContextCompat.getColor(getContext(), R.color.secondary_text));
            canvas.drawText(
                    AdminDashboardHelper.monthLabel(donor.month),
                    center,
                    monthBaseline,
                    paint);
            canvas.drawText(
                    AdminDashboardHelper.yearLabel(donor.year),
                    center,
                    yearBaseline,
                    paint);

            paint.setColor(ContextCompat.getColor(getContext(), R.color.primary_text));
            paint.setTypeface(Typeface.DEFAULT_BOLD);
            float valueBaseline = Math.max(
                    labelSize + 3 * density,
                    baseline - height - 6 * density);
            canvas.drawText(String.valueOf(donor.count), center, valueBaseline, paint);
            paint.setTypeface(Typeface.DEFAULT);
        }
    }
}