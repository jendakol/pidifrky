package cz.jenda.pidifrky.ui.api

import android.support.v4.view.GestureDetectorCompat
import android.support.v7.widget.RecyclerView
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.{MotionEvent, View}
import com.malinskiy.superrecyclerview.SuperRecyclerView

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
class RecyclerViewItemClickListener(recyclerView: SuperRecyclerView, listener: OnItemClickListener)(implicit ctx: BasicActivity) extends RecyclerView.OnItemTouchListener {

  recyclerView.addOnItemTouchListener(this)

  protected val gestureDetector = new GestureDetectorCompat(ctx, new SimpleOnGestureListener {

    override def onLongPress(e: MotionEvent): Unit = {
      val rv = recyclerView.getRecyclerView
      val childView = rv.findChildViewUnder(e.getX, e.getY)

      if (childView != null) {
        listener.onLongClick(childView, rv.getChildAdapterPosition(childView))
      }
    }

    override def onSingleTapUp(e: MotionEvent): Boolean = true
  })

  override def onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean = {
    val childView = rv.findChildViewUnder(e.getX, e.getY)

    if (childView != null && gestureDetector.onTouchEvent(e)) {
      listener.onClick(childView, rv.getChildAdapterPosition(childView))
    }

    false
  }

  override def onTouchEvent(rv: RecyclerView, e: MotionEvent): Unit = {}

  override def onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean): Unit = {}


}

object RecyclerViewItemClickListener {
  def apply(recyclerView: SuperRecyclerView, listener: OnItemClickListener)(implicit ctx: BasicActivity): RecyclerViewItemClickListener = {
    new RecyclerViewItemClickListener(recyclerView, listener)
  }
}

trait OnItemClickListener {
  def onClick(view: View, position: Int)

  def onLongClick(view: View, position: Int)
}
