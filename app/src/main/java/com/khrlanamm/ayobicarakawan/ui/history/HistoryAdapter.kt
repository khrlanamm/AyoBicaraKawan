package com.khrlanamm.ayobicarakawan.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.khrlanamm.ayobicarakawan.R
import com.khrlanamm.ayobicarakawan.databinding.ItemReportHistoryBinding
import com.khrlanamm.ayobicarakawan.ui.report.reportdata.ReportEntity // Pastikan import ini benar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryAdapter : ListAdapter<ReportEntity, HistoryAdapter.ReportViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val binding = ItemReportHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReportViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        val report = getItem(position)
        holder.bind(report)
    }

    inner class ReportViewHolder(private val binding: ItemReportHistoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(report: ReportEntity) {
            binding.tvReportName.text = report.reporterName
            binding.tvIncidentDate.text = itemView.context.getString(R.string.incident_date_prefix, report.incidentDate)
            binding.tvReportDescription.text = report.incidentDescription
            binding.tvReporterType.text = report.reporterType

            // Untuk tv_report_status, biarkan "Terkirim" seperti di XML
            binding.tvReportStatus.text = itemView.context.getString(R.string.status_terkirim) // Atau hardcode "Terkirim"

            // Mengatur warna background untuk tv_reporter_type berdasarkan tipe pelapor
            // Anda bisa menambahkan logika atau warna yang lebih spesifik di sini
            when (report.reporterType.lowercase(Locale.getDefault())) {
                itemView.context.getString(R.string.radio_korban).lowercase(Locale.getDefault()) -> {
                    binding.tvReporterType.backgroundTintList =
                        itemView.context.getColorStateList(R.color.reporter_type_victim_background) // Buat color resource ini
                }
                itemView.context.getString(R.string.radio_saksi).lowercase(Locale.getDefault()) -> {
                    binding.tvReporterType.backgroundTintList =
                        itemView.context.getColorStateList(R.color.reporter_type_witness_background) // Buat color resource ini
                }
                else -> {
                    binding.tvReporterType.backgroundTintList =
                        itemView.context.getColorStateList(R.color.default_grey) // Fallback color
                }
            }

            // Untuk iv_report_image, karena kita hanya punya imageFileName (String?)
            // dan tidak ada implementasi loading gambar dari file/URI,
            // kita bisa membiarkannya menggunakan placeholder dari XML.
            // Jika imageFileName ada, Anda bisa mencoba menampilkan sesuatu,
            // tapi untuk sekarang, placeholder sudah cukup.
            // binding.ivReportImage.visibility = if (report.imageFileName != null) View.VISIBLE else View.GONE // Contoh
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ReportEntity>() {
        override fun areItemsTheSame(oldItem: ReportEntity, newItem: ReportEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ReportEntity, newItem: ReportEntity): Boolean {
            return oldItem == newItem
        }
    }
}
