package rafal.iwanczyk.praca.inzynierska.seti.adapters

import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_member.view.*
import rafal.iwanczyk.praca.inzynierska.seti.R
import rafal.iwanczyk.praca.inzynierska.seti.models.User

open class MemberListItemsAdapter (
    private val context: Context,
    private var memberList: ArrayList<User>
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    private var onClickListener: MemberListItemsAdapter.OnClickListener? = null
    private var onLongClickListener: MemberListItemsAdapter.OnLongClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_member, parent,false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = memberList[position]

        if(holder is MyViewHolder){
            Glide
                .with(context)
                .load(model.image)
                .centerCrop()
                .placeholder(R.drawable.ic_user_place_holder)
                .into(holder.itemView.iv_member_image)

            holder.itemView.tv_member_login.text = model.login
            holder.itemView.tv_member_name.text = model.name
            holder.itemView.tv_member_email.text = model.email

            holder.itemView.setOnClickListener {
                if(onClickListener != null){
                    onClickListener!!.onClick(position,model)
                }
            }

            holder.itemView.setOnLongClickListener {
                if(onLongClickListener != null){
                    onLongClickListener!!.onLongClick(position, model)
                    holder.itemView.setBackgroundColor(Color.parseColor("#4cfc22"))
                    Handler().postDelayed({
                        holder.itemView.setBackgroundColor(context.resources.getColor(R.color.background_color))
                    }, 3500)
                    return@setOnLongClickListener true
                }
                return@setOnLongClickListener false
            }
        }
    }

    interface OnClickListener{
        fun onClick(position: Int, model: User)
    }

    fun setOnClickListener(onClickListener: OnClickListener){
        this.onClickListener = onClickListener
    }
    interface OnLongClickListener{
        fun onLongClick(position: Int, model: User)
    }

    fun setOnLongClickListener(onLongClickListener: OnLongClickListener){
        this.onLongClickListener = onLongClickListener
    }

    override fun getItemCount(): Int {
        return memberList.size
    }

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)

}