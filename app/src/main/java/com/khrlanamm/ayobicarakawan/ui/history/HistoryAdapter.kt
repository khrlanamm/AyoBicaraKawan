package com.khrlanamm.ayobicarakawan.ui.history

import android.net.Uri // Import Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View // Import View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide // Contoh menggunakan Glide, tambahkan dependensi jika belum
import com.khrlanamm.ayobicarakawan.R
import com.khrlanamm.ayobicarakawan.databinding.ItemReportHistoryBinding
import com.khrlanamm.ayobicarakawan.ui.report.reportdata.ReportEntity
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
            binding.tvReportStatus.text = itemView.context.getString(R.string.status_terkirim)

            when (report.reporterType.lowercase(Locale.getDefault())) {
                itemView.context.getString(R.string.radio_korban).lowercase(Locale.getDefault()) -> {
                    binding.tvReporterType.backgroundTintList =
                        itemView.context.getColorStateList(R.color.reporter_type_victim_background)
                }
                itemView.context.getString(R.string.radio_saksi).lowercase(Locale.getDefault()) -> {
                    binding.tvReporterType.backgroundTintList =
                        itemView.context.getColorStateList(R.color.reporter_type_witness_background)
                }
                else -> {
                    binding.tvReporterType.backgroundTintList =
                        itemView.context.getColorStateList(R.color.default_grey)
                }
            }

            // Memuat gambar menggunakan URI yang disimpan
            if (!report.imageUriString.isNullOrEmpty()) {
                try {
                    val imageUri = Uri.parse(report.imageUriString)
                    binding.ivReportImage.visibility = View.VISIBLE

                    Glide.with(itemView.context)
                        .load(imageUri)
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.error_image)
                        .into(binding.ivReportImage)

                } catch (e: Exception) {
                    Log.e("HistoryAdapter", "Error loading image URI: ${report.imageUriString}", e)
                    binding.ivReportImage.setImageResource(R.drawable.error_image) // Gambar error default
                    binding.ivReportImage.visibility = View.VISIBLE // Tetap visible dengan gambar error
                }
            } else {
                 Glide.with(itemView.context).clear(binding.ivReportImage) // Hapus gambar sebelumnya jika ada
                 binding.ivReportImage.setImageResource(R.drawable.placeholder_image)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ReportEntity>() {
        override fun areItemsTheSame(oldItem: ReportEntity, newItem: ReportEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ReportEntity, newItem: ReportEntity): Boolean {
            // Perhatikan bahwa perbandingan objek data class akan membandingkan semua propertinya
            // Jika imageUriString bisa berbeda namun item dianggap sama, sesuaikan logika ini.
            return oldItem == newItem
        }
    }
}