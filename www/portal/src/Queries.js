import React, { Component } from 'react';
import { Segment, Header, Input, Button, Icon } from 'semantic-ui-react';


class Queries extends Component {
  constructor(props) {
    super(props);
    this.state = {
      queries: [
        {
          name: "cpu_load",
          query: "SELECT avg(cpu_load) AS cpu_load"
        },
        {
          name: "power",
          query: "SELECT avg(power) AS power"
        },
      ],
      nameForm: "",
      queryForm: "",
    }
    this.onNameFormChange = this.onNameFormChange.bind(this);
    this.onQueryFormChange = this.onQueryFormChange.bind(this);
    this.onNewQuerySubmit = this.onNewQuerySubmit.bind(this);
    this.onQueryDelete = this.onQueryDelete.bind(this);
  }

  onNameFormChange(evt, data) {
    this.setState({ nameForm: data.value });
  }

  onQueryFormChange(evt, data) {
    this.setState({ queryForm: data.value });
  }

  onNewQuerySubmit() {
    const newQuery = {
      name: this.state.nameForm,
      query: this.state.queryForm,
    };
    let newQueries = this.state.queries.slice();
    newQueries.push(newQuery);
    this.setState({ queries: newQueries });
  }

  onQueryDelete = (idx) => () => {
    let newQueries = this.state.queries.slice();
    newQueries.splice(idx, 1);
    this.setState({ queries: newQueries });
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
              <Button onClick={this.onQueryDelete(idx)} style={{ marginLeft: 20 }} icon><Icon name="delete" /></Button>
            </Segment>
          ))}
        </Segment>
      </div>
    )
  }
}

export default Queries;
