var ShowWeightsSelectionPolicy = draw2d.policy.figure.SelectionPolicy.extend({
    onSelect: function(canvas, figure, isPrimarySelection) {
        if(figure instanceof Neuron) {
            figure.getInputPorts().get(0).getConnections().each(function(i, connection) {
                connection.label.setVisible(true);
            })
        }
    },

    onUnselect: function(canvas, figure) {
        // if(figure instanceof Neuron) {
        //     figure.getInputPorts().get(0).getConnections().each(function(i, connection) {
        //         connection.label.setVisible(false);
        //     })
        // }
    }
});