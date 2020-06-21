LeftEdgeLocator = draw2d.layout.locator.ConnectionLocator.extend(
    /** @lends draw2d.layout.locator.ManhattanMidpointLocator.prototype */
    {

        NAME: "draw2d.layout.locator.ManhattanMidpointLocator",

        /**
         * Constructs a ManhattanMidpointLocator with associated Connection c.
         *
         */
        init: function (attr, setter, getter) {
            this._super(attr, setter, getter)
        },


        /**
         *
         * Relocates the given Figure always in the center of an edge.
         *
         * @param {Number} index child index of the target
         * @param {draw2d.Figure} target The figure to relocate
         **/
        relocate: function (index, target) {
            var conn = target.getParent();
            var inputPort = conn.getSource();
            var neuron = inputPort.getParent();
            target.attr({
                x: neuron.getAbsoluteX() + neuron.getWidth() / 2 - target.getWidth() / 2,
                y: neuron.getAbsoluteY() + neuron.getHeight() + 5
            });
            // let points = conn.getVertices();
            //
            // let segmentIndex = Math.floor((points.getSize() - 2) / 2)
            // if (points.getSize() <= segmentIndex + 1)
            //     return
            //
            // let p1 = points.get(segmentIndex)
            // let p2 = points.get(segmentIndex + 1)
            //
            // var yShiftMultiplier = Math.abs(p1.y - p2.y) / Math.abs(p1.x - p2.x);
            // var yDirection = Math.sign(p2.y - p1.y);
            //
            // target.setPosition(
            //     p1.x + 10,
            //         p1.y + 10 * yDirection * yShiftMultiplier / 2)
                // ((p2.x - p1.x) / 2 + p1.x - target.getWidth() / 2) | 0,
                // ((p2.y - p1.y) / 2 + p1.y - target.getHeight() / 2) | 0)
        }
    });