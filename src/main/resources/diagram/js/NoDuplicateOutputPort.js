NoDuplicateOutputPort = draw2d.OutputPort.extend({
    NAME: 'NoDuplicateOutputPort',

    init: function(attr, setter, getter) {
        this._super(attr, setter, getter);
        this.connectedTo = [];
    },

    createCommand: function(request) {
        if(request.source instanceof draw2d.InputPort) {
            if(this.connectedTo.indexOf(request.source) !== -1) {
                this.setPosition(this.ox, this.oy);
                return null;
            } else {
                this.connectedTo.push(request.source);
                return this._super(request);
            }
        } else {
            return this._super(request);
        }
    },

    onDisconnect: function(connection) {
        let index = this.connectedTo.indexOf(connection.targetPort);
        if(index !== -1) {
            this.connectedTo.splice(index, 1);
        }
        return this._super(connection);
    }
});