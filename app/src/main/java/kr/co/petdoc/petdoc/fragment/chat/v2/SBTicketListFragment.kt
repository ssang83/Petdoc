package kr.co.petdoc.petdoc.fragment.chat.v2

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sendbird.android.*
import com.sendbird.desk.android.Ticket
import kotlinx.android.synthetic.main.adapter_ticket_item.view.*
import kotlinx.android.synthetic.main.fragment_pet_breed.*
import kotlinx.android.synthetic.main.fragment_sb_ticket_list.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.chat.v2.SBChatMessageActivity
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.fragment.chat.v2.desk.DeskAdminMessage
import kr.co.petdoc.petdoc.fragment.chat.v2.desk.DeskManager
import kr.co.petdoc.petdoc.fragment.chat.v2.desk.DeskManager.Companion.CONNECTION_HANDLER_ID_OPEN_TICKETS
import kr.co.petdoc.petdoc.fragment.chat.v2.desk.DeskManager.Companion.TICKET_HANDLER_ID_OPEN_TICKETS
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.utils.DateUtils
import kr.co.petdoc.petdoc.utils.image.ImageUtil
import kr.co.petdoc.petdoc.utils.image.StorageUtils

/**
 * Petdoc
 * Class: SBChannelListFragment
 * Created by kimjoonsung on 12/4/20.
 *
 * Description :
 */
class SBTicketListFragment : Fragment() {

    private lateinit var ticketAdapter:TicketAdapter
    private var mTicketList:MutableList<Ticket> = mutableListOf()

    private var mOffset = 0
    private var mHasNext = true
    private var mLoading = false

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sb_ticket_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnChat.setOnClickListener { createChat() }

        //==========================================================================================

        ticketAdapter = TicketAdapter()
        ticketList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = ticketAdapter

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    ticketList.layoutManager?.let { lm ->
                        val first = (lm as LinearLayoutManager).findFirstVisibleItemPosition()
                        val visibleCount = lm.childCount
                        val totalCount = lm.itemCount

                        if (first + visibleCount >= (totalCount * 0.8f).toInt()) {
                            loadOpenTicket(false)
                        }
                    }
                }
            })
        }

        //=========================================================================================

        if (StorageUtils.loadBooleanValueFromPreference(requireContext(), AppConstants.PREF_KEY_LOGIN_STATUS)) {
            layoutChatList.visibility = View.VISIBLE
            layoutLogin.visibility = View.GONE
        } else {
            layoutChatList.visibility = View.GONE
            layoutLogin.visibility = View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()

        DeskManager.addTicketHandler(TICKET_HANDLER_ID_OPEN_TICKETS, object : DeskManager.TicketHandler() {
            override fun onMessageReceived(baseChannel: BaseChannel, baseMessage: BaseMessage) {
                if (DeskAdminMessage.isMessage(baseMessage)) {
                    if (DeskAdminMessage.isAssignType(baseMessage) || DeskAdminMessage.isTransferType(baseMessage)) {
                        // Updates ticket information without sorting.
                        Ticket.getByChannelUrl(baseChannel.url, { ticket, exception ->
                            if (exception != null) {
                                return@getByChannelUrl
                            }

                            if (ticket != null) {
                                ticketAdapter.replace(ticket)
                                ticketAdapter.notifyDataSetChanged()
                            }
                        })
                    } else if (DeskAdminMessage.isCloseType(baseMessage)) {
                        // Moves ticket to closed ticket list.
                        Ticket.getByChannelUrl(baseChannel.url, { ticket, sendBirdException ->
                            if (sendBirdException != null) {
                                return@getByChannelUrl
                            }

                            if (ticket != null) {
                                ticketAdapter.remove(ticket.id)
                                ticketAdapter.notifyDataSetChanged()

                                if (mTicketList.size == 0) {
                                    loadOpenTicket(true)
                                }
                            }
                        })
                    }
                }
            }

            override fun onChannelChanged(baseChannel: BaseChannel) {
                // Each ticket has serialized channel object (which is not singleton).
                // Therefore, ticket should be updated with the updated channel to match the unread message count.
                processMessage(baseChannel)
            }

            override fun onMessageUpdated(baseChannel: BaseChannel, message: BaseMessage) {}

            private fun processMessage(channel: BaseChannel) {
                Ticket.getByChannelUrl(channel.url, { ticket, exception ->
                    if (exception != null) {
                        return@getByChannelUrl
                    }

                    if (ticket != null && ticket.channel != null) {
                        var updatedTicket: Ticket? = null
                        var ticketChannerUrl = ""

                        for (t in mTicketList) {
                            ticketChannerUrl = if (t.channel != null) t.channel.url else ""
                            if (ticketChannerUrl == ticket.channel.url) {
                                ticketAdapter.remove(t.id)
                                updatedTicket = ticket
                                break
                            }
                        }

                        if (updatedTicket != null) {
                            ticketAdapter.insert(ticket, 0)
                            ticketAdapter.notifyDataSetChanged()
                        } else {
                            loadOpenTicket(true)
                        }
                    }
                })
            }
        })

        SBConnectionManager.addConnectionManagementHandler(requireContext(), CONNECTION_HANDLER_ID_OPEN_TICKETS, object : SBConnectionManager.ConnectionManagementHandler {
            override fun onConnected(reconnect: Boolean) {
                loadOpenTicket(true)
            }
        })
    }

    override fun onPause() {
        super.onPause()
        DeskManager.removeTicketHandler(TICKET_HANDLER_ID_OPEN_TICKETS)
        SBConnectionManager.removeConnectionManagementHandler(CONNECTION_HANDLER_ID_OPEN_TICKETS)
    }

    //==============================================================================================

    private fun createChat() {
        startActivity(Intent(requireActivity(), SBChatMessageActivity::class.java).apply {
            putExtra(SBChatMessageActivity.EXTRA_TITLE, "#${System.currentTimeMillis()}")
            putExtra(SBChatMessageActivity.EXTRA_USER_NAME, StorageUtils.loadValueFromPreference(requireContext(), AppConstants.PREF_KEY_USER_ID, ""))
            setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }

    private fun loadOpenTicket(refresh: Boolean) {
        if (refresh) {
            mOffset = 0
            mHasNext = true
            mLoading = false
        }

        if(!mHasNext || mLoading) return

        mLoading = true

        Ticket.getOpenedList(mOffset, { tickets: MutableList<Ticket>, hasNext: Boolean, e: SendBirdException? ->
            if (e != null) {
                mLoading = false

                if (SendBird.getConnectionState() == SendBird.ConnectionState.OPEN) {
                    Logger.d("Loading open tickets failed.")
                }

                return@getOpenedList
            }

            mLoading = false

            if (tickets.size == 0) {
                layoutEmpty.visibility = View.VISIBLE
                ticketList.visibility = View.GONE

                mTicketList.clear()
            } else {
                mOffset += tickets.size
                mHasNext = hasNext

                layoutEmpty.visibility = View.GONE
                ticketList.visibility = View.VISIBLE

                if (refresh) {
                    tickets.clear()
                }

                mTicketList.addAll(tickets)
                ticketAdapter.notifyDataSetChanged()
            }
        })
    }

    private fun onTicketClicked(ticket: Ticket) {
        startActivity(Intent(requireActivity(), SBChatMessageActivity::class.java).apply {
            putExtra(SBChatMessageActivity.EXTRA_TITLE, ticket.title)
            putExtra(SBChatMessageActivity.EXTRA_CHANNEL_URL, ticket.channel.url)
        })
    }

    //==============================================================================================
    inner class TicketAdapter : RecyclerView.Adapter<TicketHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
                TicketHolder(layoutInflater.inflate(R.layout.adapter_ticket_item, parent, false))

        override fun onBindViewHolder(holder: TicketHolder, position: Int) {
            holder.bind(mTicketList[position])
        }

        override fun getItemCount() = mTicketList.size

        fun insert(item: Ticket, position: Int) {
            mTicketList.add(position, item)
        }

        fun remove(id: Long) {
            for (ticket in mTicketList) {
                if (ticket.id == id) {
                    mTicketList.remove(ticket)
                    break
                }
            }
        }

        fun replace(item: Ticket) {
            var index = -1
            for (ticket in mTicketList) {
                if (ticket.id == item.id) {
                    index = mTicketList.indexOf(ticket)
                    mTicketList.remove(ticket)
                    break
                }
            }

            if (index != -1) {
                mTicketList.add(index, item)
            }
        }
    }

    inner class TicketHolder(var _view: View) : RecyclerView.ViewHolder(_view) {
        fun bind(ticket: Ticket) {
            _view.setOnClickListener { onTicketClicked(ticket) }

            _view.txt_title.text = ticket.title

            val message = DeskManager.getLastMessage(ticket)
            if (message != null) {
                _view.txt_last_update.text = DateUtils.formatDateTime(message.createdAt)
                _view.txt_last_message.text = message.message

                _view.txt_unread_count.apply {
                    when {
                        DeskManager.getUnreadMessageCount(ticket) > 9 -> {
                            visibility = View.VISIBLE
                            text = "9+"
                        }

                        DeskManager.getUnreadMessageCount(ticket) > 0 -> {
                            visibility = View.VISIBLE
                            text = DeskManager.getUnreadMessageCount(ticket).toString()
                        }

                         else -> {
                            visibility = View.INVISIBLE
                        }
                    }
                }
            } else {
                _view.txt_last_update.text = ""
                _view.txt_last_message.text = ""
                _view.txt_unread_count.visibility = View.INVISIBLE
            }

            if (ticket.agent != null) {
                ImageUtil.displayImageFromUrlWithPlaceHolder(
                        requireContext(),
                        ticket.agent.profileUrl,
                        _view.img_agent_profile,
                        R.drawable.profile_default
                )

                _view.txt_agent_name.text = ticket.agent.name
            } else {
                _view.img_agent_profile.setBackgroundResource(R.drawable.profile_default)
                _view.txt_agent_name.text = ""
            }
        }
    }

    interface OnTicketClosedListener {
        fun onTicketClosed(ticket: Ticket)
    }
}