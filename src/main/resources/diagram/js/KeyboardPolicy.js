KeyboardPolicy = draw2d.policy.canvas.KeyboardPolicy.extend({
    init: function () {
        this._super();
    },
    onKeyDown: function (canvas, keyCode, shiftKey, ctrlKey) {
        //
        if (keyCode === 46 && canvas.getPrimarySelection() !== null) {
            // create a single undo/redo transaction if the user delete more than one element.
            // This happens with command stack transactions.
            //
            canvas.getCommandStack().startTransaction(draw2d.Configuration.i18n.command.deleteShape)
            let selection = canvas.getSelection()
            selection.each(function (index, figure) {
                // don't delete a connection if the source or target figure is part of the selection.
                // In this case the connection is deleted by the DeleteCommand itself and it is not allowed to
                // delete a figure twice.
                //
                if (figure instanceof draw2d.Connection) {
                    if (selection.contains(figure.getSource(), true)) {
                        return
                    }
                    if (selection.contains(figure.getTarget(), true)) {
                        return
                    }
                } else if (figure instanceof Tiers){
                    return
                }
                let cmd = figure.createCommand(new draw2d.command.CommandType(draw2d.command.CommandType.DELETE))
                if (cmd !== null) {
                    canvas.getCommandStack().execute(cmd)
                }
            })
            // execute all single commands at once.
            canvas.getCommandStack().commitTransaction()
        }
        else {
            this._super(canvas, keyCode, shiftKey, ctrlKey)
        }

    }
});