import React, { Component } from 'react';
import { Segment, Header, Input, Button, Icon } from 'semantic-ui-react';
import axios from 'axios';


class Queries extends Component {
  constructor(props) {
    super(props);
    this.state = {
      queries: [],
      nameForm: "",
      queryForm: "",
    }
    this.fetchQueries = this.fetchQueries.bind(this);
    this.onNameFormChange = this.onNameFormChange.bind(this);
    this.onQueryFormChange = this.onQueryFormChange.bind(this);
    this.onNewQuerySubmit = this.onNewQuerySubmit.bind(this);
    this.onQueryDelete = this.onQueryDelete.bind(this);
  }

  componentWillMount() {
    this.fetchQueries();
  }

  fetchQueries() {
    axios.get(this.props.agentAddress + '/get_queries').then(res => {
      this.setState({ queries: res.data.queries });
    });
  }

  onNameFormChange(evt, data) {
    this.setState({ nameForm: data.value });
  }

  onQueryFormChange(evt, data) {
    this.setState({ queryForm: data.value });
  }

  onNewQuerySubmit() {
    const newQuery = {
      'name': "&" + this.state.nameForm,
      'query': this.state.queryForm,
    };
    axios({
      method: 'post',
      url: this.props.agentAddress + "/install_query",
      data: newQuery,
      headers: {'Content-Type': 'application/json'},
    }).then(res => {
      this.fetchQueries();
    })
  }

  onQueryDelete = (qName) => () => {
    axios({
      method: 'post',
      url: this.props.agentAddress + "/uninstall_query",
      data: {'name': qName},
      headers: {'Content-Type': 'application/json'},
    }).then(res => {
      this.fetchQueries();
    })
  }

  render() {
    return (
      <div>
        <Segment>
          <Header as='h3'>Queries</Header>
          <Segment>
            <p>Add new query:</p>
            <Input placeholder="name" onChange={this.onNameFormChange} />
            <Input placeholder="query" onChange={this.onQueryFormChange} />
            <Button onClick={this.onNewQuerySubmit}>Submit</Button>
          </Segment>
          {this.state.queries.map((q, idx) => (
            <Segment key={idx}>
              {q.name + ": " + q.query}
              <Button onClick={this.onQueryDelete(q.name)} style={{ marginLeft: 20 }} icon><Icon name="delete" /></Button>
            </Segment>
          ))}
        </Segment>
      </div>
    )
  }
}

export default Queries;
