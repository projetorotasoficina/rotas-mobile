package br.edu.utfpr.geocoleta.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import br.edu.utfpr.geocoleta.Data.Models.Route
import br.edu.utfpr.geocoleta.R

class RouteAdapter(
    private var routes: List<Route>,
    private val onItemClick: (Route) -> Unit
) : RecyclerView.Adapter<RouteAdapter.RouteViewHolder>() {

    inner class RouteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nomeTextView: TextView = itemView.findViewById(R.id.tvTituloRota)

        fun bind(route: Route) {
            nomeTextView.text = route.nome
            itemView.setOnClickListener { onItemClick(route) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RouteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_rote, parent, false)
        return RouteViewHolder(view)
    }

    override fun onBindViewHolder(holder: RouteViewHolder, position: Int) {
        val route = routes[position]
        holder.bind(route)
    }

    override fun getItemCount(): Int = routes.size

    fun updateList(newRoutes: List<Route>) {
        routes = newRoutes
        notifyDataSetChanged()
    }
}