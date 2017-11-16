import React, { Component } from 'react';
import WelcomeMenu from './WelcomeMenu';
import AgentMenu from './AgentMenu';
import './App.css';

class App extends Component {
  constructor(props) {
    super(props);
    this.state = {agent: null};
    this.onAgentChange = this.onAgentChange.bind(this);
  }

  onAgentChange(agent) {
    this.setState({
      agent,
    });
  }

  render() {
    return (
      <div className="App">
        <header className="App-header">
          <h1 className="App-title">CloudAtlas Portal</h1>
        </header>
        {this.state.agent && <AgentMenu agent={this.state.agent} />}
        {!this.state.agent && <WelcomeMenu agentConnectCb={this.onAgentChange} />}
      </div>
    );
  }
}

export default App;
