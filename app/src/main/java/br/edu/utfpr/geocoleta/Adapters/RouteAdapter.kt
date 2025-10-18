package br.edu.utfpr.geocoleta.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import br.edu.utfpr.geocoleta.Data.Models.Route
import br.edu.utfpr.geocoleta.R

class RouteAdapter(
    private val listaRotas: List<Route>,
    private val onItemClick: (Route) -> Unit
) : RecyclerView.Adapter<RouteAdapter.RotaViewHolder>() {

    inner class RotaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitulo: TextView = itemView.findViewById(R.id.tvTituloRota)
        val tvDescricao: TextView = itemView.findViewById(R.id.tvDescricaoRota)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RotaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_rote, parent, false)
        return RotaViewHolder(view)
    }

    override fun onBindViewHolder(holder: RotaViewHolder, position: Int) {
        val rota = listaRotas[position]
//        holder.tvTitulo.text = rota.titulo
//        holder.tvDescricao.text = rota.descricao

        // Clique no card
        holder.itemView.setOnClickListener {
            onItemClick(rota)
        }
    }

    override fun getItemCount(): Int = listaRotas.size
}