package com.ammaia_ispc.sangreyamobile.helpers;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
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
    }

    public void setMonthlyDonors(List<DashboardMonthlyDonors> monthlyDonors) {
        donutChart = false;
        this.monthlyDonors = monthlyDonors;
        invalidate();
    }

    public void setCampaignStatuses(List<DashboardCampaignStatus> campaignStatuses) {
        donutChart = true;
        this.campaignStatuses = campaignStatuses;
        invalidate();
    }

    public void setEmptyMessage(String emptyMessage) {
        this.emptyMessage = emptyMessage == null ? "" : emptyMessage;
        invalidate();
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
                float sweep = total == 0 ? 0 : status.cantidad * 360f / total;
                paint.setColor(ContextCompat.getColor(
                        getContext(),
                        CampaignHelper.statusColor(status.estado)));
                canvas.drawArc(bounds, startAngle, sweep, false, paint);
                startAngle += sweep;
            }
        }

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(ContextCompat.getColor(getContext(), R.color.primary_text));
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setTextSize(22 * density);
        canvas.drawText(String.valueOf(total), centerX, centerY + 3 * density, paint);
        paint.setTypeface(Typeface.DEFAULT);
        paint.setTextSize(10 * density);
        canvas.drawText(
                getContext().getString(R.string.dashboard_campaigns_label),
                centerX,
                centerY + 19 * density,
                paint);
    }

    private void drawBars(Canvas canvas) {
        if (monthlyDonors.isEmpty()) {
            if (!emptyMessage.isEmpty()) {
                float density = getResources().getDisplayMetrics().density;
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(ContextCompat.getColor(getContext(), R.color.secondary_text));
                paint.setTextAlign(Paint.Align.CENTER);
                paint.setTypeface(Typeface.DEFAULT);
                paint.setTextSize(12 * density);
                canvas.drawText(emptyMessage, getWidth() / 2f, getHeight() / 2f, paint);
            }
            return;
        }

        float density = getResources().getDisplayMetrics().density;
        float left = 14 * density;
        float right = getWidth() - 8 * density;
        float top = 24 * density;
        float baseline = getHeight() - 28 * density;
        float chartHeight = baseline - top;
        int max = 0;
        for (DashboardMonthlyDonors donor : monthlyDonors) {
            max = Math.max(max, donor.cantidad);
        }

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(density);
        paint.setColor(ContextCompat.getColor(getContext(), R.color.border));
        canvas.drawLine(left, baseline, right, baseline, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(Typeface.DEFAULT);
        paint.setTextSize(9 * density);

        float slotWidth = (right - left) / monthlyDonors.size();
        float barWidth = Math.min(27 * density, slotWidth * 0.58f);
        for (int index = 0; index < monthlyDonors.size(); index++) {
            DashboardMonthlyDonors donor = monthlyDonors.get(index);
            float height = max == 0 ? 0 : chartHeight * donor.cantidad / max;
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
            float monthBaseline = getHeight() - 17 * density;
            float yearBaseline = getHeight() - 5 * density;
            canvas.drawText(
                    AdminDashboardHelper.monthLabel(donor.mes),
                    center,
                    monthBaseline,
                    paint);
            canvas.drawText(
                    AdminDashboardHelper.yearLabel(donor.anio),
                    center,
                    yearBaseline,
                    paint);

            paint.setColor(ContextCompat.getColor(getContext(), R.color.primary_text));
            paint.setTypeface(Typeface.DEFAULT_BOLD);
            float valueBaseline = Math.max(
                    12 * density,
                    baseline - height - 6 * density);
            canvas.drawText(String.valueOf(donor.cantidad), center, valueBaseline, paint);
            paint.setTypeface(Typeface.DEFAULT);
        }
    }
}
