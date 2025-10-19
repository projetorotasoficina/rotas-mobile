package br.edu.utfpr.geocoleta.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import br.edu.utfpr.geocoleta.Models.Truck
import br.edu.utfpr.geocoleta.R

class TruckAdapter(
    private var listaTrucks: List<Truck>,
    private val onItemClick: (Truck) -> Unit
) : RecyclerView.Adapter<TruckAdapter.TruckViewHolder>() {

    private var selectedPosition: Int = RecyclerView.NO_POSITION

    inner class TruckViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvPlaca: TextView = itemView.findViewById(R.id.tvPlaca)
        val tvDescricao: TextView = itemView.findViewById(R.id.tvDescricao)
        val card: LinearLayout = itemView as LinearLayout
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TruckViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_truck, parent, false)
        return TruckViewHolder(view)
    }

    override fun onBindViewHolder(holder: TruckViewHolder, position: Int) {
        val truck = listaTrucks[position]
        holder.tvPlaca.text = "PLACA ${truck.placa}"
        holder.tvDescricao.text = truck.descricao

        // Fundo normal ou selecionado
        if (position == selectedPosition) {
//            holder.card.setBackgroundResource(R.drawable.bg_card_selected)
        } else {
//            holder.card.setBackgroundResource(R.drawable.bg_card)
        }

        holder.itemView.setOnClickListener {
            val previousPosition = selectedPosition
            selectedPosition = position
            notifyItemChanged(previousPosition)
            notifyItemChanged(selectedPosition)
            onItemClick(truck)
        }
    }

    override fun getItemCount(): Int = listaTrucks.size

    // Atualiza lista (para busca)
    fun updateList(newList: List<Truck>) {
        listaTrucks = newList
        selectedPosition = RecyclerView.NO_POSITION
        notifyDataSetChanged()
    }
}