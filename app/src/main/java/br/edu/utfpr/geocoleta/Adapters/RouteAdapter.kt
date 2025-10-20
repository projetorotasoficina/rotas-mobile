package br.edu.utfpr.geocoleta.Adapters

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import br.edu.utfpr.geocoleta.Data.Models.Route
import br.edu.utfpr.geocoleta.R

class RouteAdapter(
    private var routes: List<Route>,
    private val onItemClick: (Route) -> Unit
) : RecyclerView.Adapter<RouteAdapter.RouteViewHolder>() {

    private var selectedPosition: Int = RecyclerView.NO_POSITION

    inner class RouteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nomeTextView: TextView = itemView.findViewById(R.id.tvTituloRota)
        val ivIcon: ImageView = itemView.findViewById(R.id.ivIcon)
        val card: CardView = itemView as CardView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RouteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_rote, parent, false)
        return RouteViewHolder(view)
    }

    override fun onBindViewHolder(holder: RouteViewHolder, position: Int) {
        val route = routes[position]
        holder.nomeTextView.text = route.nome

        if (position == selectedPosition) {
            holder.card.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.primary_light))
            holder.ivIcon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(holder.itemView.context, R.color.primary_blue))
        } else {
            holder.card.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.card))
            holder.ivIcon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(holder.itemView.context, R.color.default_icon_color))
        }

        holder.itemView.setOnClickListener {
            val previousPosition = selectedPosition
            selectedPosition = holder.adapterPosition
            notifyItemChanged(previousPosition)
            notifyItemChanged(selectedPosition)
            onItemClick(route)
        }
    }

    override fun getItemCount(): Int = routes.size

    fun updateList(newRoutes: List<Route>) {
        routes = newRoutes
        selectedPosition = RecyclerView.NO_POSITION
        notifyDataSetChanged()
    }
}