package uk.co.epicuri.waiter.ui.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.os.Bundle

class MultiSelectDialog : DialogFragment(){

    var onItemsSelectedListener: OnItemsSelectedListener? = null
    var items: ArrayList<String> = ArrayList()
    var selectedItems: ArrayList<String> = ArrayList()
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val builder = AlertDialog.Builder(activity)

        val checkedItems: BooleanArray = kotlin.BooleanArray(items.size)
        if (selectedItems.size == 0) {
            checkedItems[0] = false
        } else {
            for ((i, item) in items.withIndex()) {
                checkedItems[i] = selectedItems.contains(item)
            }
        }

        builder.setTitle("Please Select")
                .setMultiChoiceItems(items.toTypedArray(), checkedItems) { dialog, item, isChecked ->
                    run {
                        if((dialog as AlertDialog).listView.isItemChecked(0)){
                            for( i in 1..(checkedItems.size - 1)){
                                checkedItems[i] = false
                                dialog.listView.setItemChecked(i, false)
                                selectedItems.clear()

                            }
                            return@setMultiChoiceItems
                        }

                        if (isChecked) {
                            selectedItems.add(items[item])
                        } else {
                            if (selectedItems.contains(items[item])) selectedItems.remove(items[item])
                        }
                        return@setMultiChoiceItems
                    }
                }
                .setPositiveButton("Done") { dialog, _ ->
                    run {
                        if ((dialog as AlertDialog).listView.isItemChecked(0)) selectedItems.clear()
                        onItemsSelectedListener?.onItemsSelectedListener(selectedItems)
                    }
                }
        return builder.create()
    }


    interface OnItemsSelectedListener {
        fun onItemsSelectedListener(items: ArrayList<String>)
    }
}