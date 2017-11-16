import React, { Component } from 'react';
import { Segment, Header, Input, Button } from 'semantic-ui-react';
import PropTypes from 'prop-types';


class WelcomeMenu extends Component {
  constructor(props) {
    super(props);
    this.state = {
      error: null,
      agentAddress: "http://localhost:5002",
    }
    this.onAgentConnectRequest = this.onAgentConnectRequest.bind(this);
    this.onAgentAddressChange = this.onAgentAddressChange.bind(this);
  }

  onAgentConnectRequest() {
    this.props.agentConnectCb(this.state.agentAddress);
  }

  onAgentAddressChange(event, data) {
    this.setState({ agentAddress: data.value });
  }

  render() {
    return (
      <Segment>
        <Header as='h1'>Welcome to CloudAtlas!</Header>
        <p>Enter Agent address:</p>
        <Input defaultValue='http://localhost:5002' placeholder='Agent address' onChange={this.onAgentAddressChange}/>
        <Button loading={false} onClick={this.onAgentConnectRequest}>Connect</Button>
      </Segment>
    );
  }
}

WelcomeMenu.propTypes = {
  agentConnectCb: PropTypes.func.isRequired,
};

export default WelcomeMenu;
