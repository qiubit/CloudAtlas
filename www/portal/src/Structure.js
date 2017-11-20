import React, { Component } from 'react';
import { Segment, Header } from 'semantic-ui-react';
import { Line } from 'react-chartjs-2';
import axios from 'axios';


class StructureTree extends Component {
  render() {
    return (
      <div>
        {
          this.props.tree.map(
            (zoneObj, idx) => (
              <Segment key={idx}>
                <Header as='h3'>{zoneObj.zone}</Header>
                {
                  zoneObj.attributes.map(
                    (attr, idx) => (
                      <p
                        style={{ color: 'darkblue', textDecoration: 'underline' }}
                        onClick={() => { this.props.activeChartChangeCb(zoneObj.zone, attr) }}
                        key={idx}>
                        {attr.name + ": " + attr.value}
                      </p>
                    )
                  )
                }
              </Segment>
            )
          )
        }
      </div>
    );
  }
}

class Structure extends Component {
  constructor(props) {
    super(props);

    this.state = {
      structure: [],
      generated: [],
      activeChart: {
        zone: null,
        attribute: null,
      },
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
    this.zoneFetcher = this.zoneFetcher.bind(this);
    this.onChartDataArrived = this.onChartDataArrived.bind(this);
    this.onZoneDataArrived = this.onZoneDataArrived.bind(this);
    this.onZoneDataSet = this.onZoneDataSet.bind(this);
    this.onZoneDataFetched = this.onZoneDataFetched.bind(this);
    this.onChangeActiveChart = this.onChangeActiveChart.bind(this);
  }

  componentDidMount() {
    this.zoneFetcher();
    this.dataFetcher = setInterval(() => { this.zoneFetcher() }, 10000);
  }

  componentWillUnmount() {
    clearInterval(this.dataFetcher);
  }

  zoneFetcher() {
    axios.get(this.props.agentAddress + "/zones").then(res => {
      this.onZoneDataArrived(res.data.zones);
    })
  }

  onZoneDataArrived(zoneData) {
    this.setState(
      { zones: zoneData },
      this.onZoneDataSet
    )
  }

  onZoneDataSet() {
    this.state.zones.forEach((zone) => {
      this.fetchZoneData(zone);
    })
  }

  fetchZoneData(zone) {
    axios({
      method: 'post',
      url: this.props.agentAddress + "/get_attributes",
      data: {
        'zone': zone,
      },
      headers: {'Content-Type': 'application/json'},
    }).then(res => {
      this.onZoneDataFetched(zone, res.data.attributes);
    })
  }

  onDataArrived(data) {
    this.state.chartData.labels.push(this.state.chartData.labels.length);
    this.state.chartData.datasets[0].data.push(data);
    this.refs.chart.chart_instance.update();
  }

  onZoneDataFetched(zone, attributes) {
    let zoneDataExists = false;
    let structure = this.state.structure.slice();
    structure.forEach((dataPt, idx) => {
      if (dataPt.zone === zone) {
        zoneDataExists = true;
        structure[idx] = { zone: zone, attributes: attributes };
      }
    });
    if (!zoneDataExists)
      structure.push({ zone: zone, attributes: attributes });
    if (zone === this.state.activeChart.zone) {
        attributes.forEach((attr) => {
          if (attr.name === this.state.activeChart.attribute.name) {
            this.onChartDataArrived(attr.value);
          }
        });
      }
    this.setState({ structure: structure });
  }

  onChartDataArrived(data) {
    this.state.chartData.labels.push(this.state.chartData.labels.length);
    this.state.chartData.datasets[0].data.push(data);
    this.refs.chart.chart_instance.update();
  }

  onChangeActiveChart(zone, attribute) {
    // TODO: Fix bug where after second display of same attribute, it won't plot it
    let chartDataPlaceholder = {
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
    chartDataPlaceholder.chartData.datasets[0].label = attribute.name;
    this.setState({ activeChart: { zone: zone, attribute: attribute }, chartData: chartDataPlaceholder.chartData });
  }

  render() {
    return (
      <div>
        <StructureTree
          activeChartChangeCb={this.onChangeActiveChart}
          tree={this.state.structure}
        />
        { this.state.activeChart.attribute && (
        <Segment>
          <Header as='h3'>{this.state.activeChart.zone}: {this.state.activeChart.attribute.name} live chart</Header>
          <Line ref='chart' data={this.state.chartData} />
        </Segment>
        )}
      </div>
    )
  }
}

export default Structure;
