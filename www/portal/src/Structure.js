import React, { Component } from 'react';
import { Segment, Header, Menu, Input, Button } from 'semantic-ui-react';
import PropTypes from 'prop-types';
import { Line } from 'react-chartjs-2';


class StructureTree extends Component {
  nodeToPath(node) {
    let name = "/" + node.name;
    while (node.parent != null) {
      node = node.parent;
      if (node.name !== "") {
        name = node.name + "/" + name;
      }
    }
    return name;
  }

  nodeToAttributes(node) {
    let attributes = [];
    for (var property in node) {
      if (node.hasOwnProperty(property)) {
        attributes.push(<p>{property + ": " + node[property]}</p>)
      }
    }
    return attributes;
  }

  render() {
    console.log(this.props.tree.length);
    return (
      <div>
        {this.props.tree.map((level) => {
          return level.map((node) => (
            <Segment>
              <Header as='h3'>{this.nodeToPath(node)}</Header>
              {this.nodeToAttributes(node)}
            </Segment>
          ));
        })}
      </div>
    );
  }
}

class Structure extends Component {
  constructor(props) {
    super(props);

    const rootZone = {
      name: "",
      isActive: "true",
      cpu: "0.8",
      parent: null,
    };
    const uwZone = {
      name: "uw",
      isActive: "true",
      cpu: "0.4",
      parent: rootZone,
    };
    const pjwstk = {
      name: "pjwstk",
      isActive: "true",
      cpu: "0.3",
      parent: rootZone,
    }

    this.state = {
      structure: [
        [
          rootZone,
        ],
        [
          uwZone,
          pjwstk,
        ],
      ],
      generated: [],
      chartData: {
        labels: [],
        datasets: [
          {
            label: 'Attribute',
            fill: false,
            lineTension: 0.1,
            backgroundColor: 'rgba(75,192,192,0.4)',
            borderColor: 'rgba(75,192,192,1)',
            borderCapStyle: 'butt',
            borderDash: [],
            borderDashOffset: 0.0,
            borderJoinStyle: 'miter',
            pointBorderColor: 'rgba(75,192,192,1)',
            pointBackgroundColor: '#fff',
            pointBorderWidth: 1,
            pointHoverRadius: 5,
            pointHoverBackgroundColor: 'rgba(75,192,192,1)',
            pointHoverBorderColor: 'rgba(220,220,220,1)',
            pointHoverBorderWidth: 2,
            pointRadius: 1,
            pointHitRadius: 10,
            data: []
          }
        ]
      }
    };
    this.onDataArrived = this.onDataArrived.bind(this);
  }

  componentDidMount() {
    this.chartUpdater = setInterval(() => { this.onDataArrived(Math.random())}, 1000);
  }

  componentWillUnmount() {
    clearInterval(this.chartUpdater);
  }

  onDataArrived(data) {
    this.state.chartData.labels.push(this.state.chartData.labels.length);
    this.state.chartData.datasets[0].data.push(data);
    this.refs.chart.chart_instance.update();
  }

  render() {
    return (
      <div>
        <StructureTree tree={this.state.structure} />
        <Segment>
          <Header as='h3'>Attribute Live Chart</Header>
          <Line ref='chart' data={this.state.chartData} />
        </Segment>
      </div>
    )
  }
}

export default Structure;
