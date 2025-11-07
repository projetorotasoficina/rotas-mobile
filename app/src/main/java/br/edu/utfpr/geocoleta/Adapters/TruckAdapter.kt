package br.edu.utfpr.geocoleta.Adapters

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import br.edu.utfpr.geocoleta.Data.Models.Truck
import br.edu.utfpr.geocoleta.R

class TruckAdapter(
    private var listaTrucks: List<Truck>,
    private val onItemClick: (Truck) -> Unit
) : RecyclerView.Adapter<TruckAdapter.TruckViewHolder>() {

    private var selectedPosition: Int = RecyclerView.NO_POSITION

    inner class TruckViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvPlaca: TextView = itemView.findViewById(R.id.tvPlaca)
        val tvDescricao: TextView = itemView.findViewById(R.id.tvDescricao)
        val ivIcon: ImageView = itemView.findViewById(R.id.ivIcon)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        val card: CardView = itemView as CardView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TruckViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_truck, parent, false)
        return TruckViewHolder(view)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: TruckViewHolder, position: Int) {
        val truck = listaTrucks[position]
        holder.tvPlaca.text = "PLACA ${truck.placa}"
        holder.tvDescricao.text = truck.modelo

        if (truck.ativo) {
            holder.tvStatus.text = "DISPONÍVEL"
            holder.tvStatus.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.white))
            holder.tvStatus.setBackgroundResource(R.drawable.badge_success)
            holder.itemView.alpha = 1.0f
            holder.itemView.isClickable = true
        } else {
            holder.tvStatus.text = "INDISPONÍVEL"
            holder.tvStatus.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.white))
            holder.tvStatus.setBackgroundResource(R.drawable.badge_error)
            holder.itemView.alpha = 0.5f
            holder.itemView.isClickable = false
        }

        if (position == selectedPosition) {
            holder.card.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.primary_light))
            holder.ivIcon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(holder.itemView.context, R.color.primary_blue))
        } else {
            holder.card.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.card))
            holder.ivIcon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(holder.itemView.context, R.color.default_icon_color))
        }

        holder.itemView.setOnClickListener {
            if (truck.ativo) {
                val previousPosition = selectedPosition
                selectedPosition = holder.adapterPosition
                notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)
                onItemClick(truck)
            }
        }
    }

    override fun getItemCount(): Int = listaTrucks.size

    fun updateList(newList: List<Truck>) {
        listaTrucks = newList
        selectedPosition = RecyclerView.NO_POSITION
        notifyDataSetChanged()
    }
}