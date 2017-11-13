import React, { Component } from 'react';
import { Segment, Header, Menu } from 'semantic-ui-react';
import PropTypes from 'prop-types';
import Contacts from './Contacts';
import Structure from './Structure';


class AgentMenu extends Component {
  constructor(props) {
    super(props);
    this.PageEnum = { MENU: 1, STRUCTURE: 2, QUERIES: 3, CONTACTS: 4 };
    this.state = {
      currentPage: this.PageEnum.MENU,
    }
    this.onPageChange = this.onPageChange.bind(this);
    this.handleItemClick = this.handleItemClick.bind(this);
    this.handleAgentNameClick = this.handleAgentNameClick.bind(this);
  }

  onPageChange(pageId) {
    this.setState({ currentPage: pageId });
  }

  handleItemClick = (e, { name }) => {
    switch (name) {
      case 'ZMI Structure':
        this.setState({ currentPage: this.PageEnum.STRUCTURE });
        break;
      case 'Queries':
        this.setState({ currentPage: this.PageEnum.QUERIES });
        break;
      case 'Contacts':
        this.setState({ currentPage: this.PageEnum.CONTACTS });
        break;
      default:
        this.setState({ currentPage: this.PageEnum.MENU });
    }
  }

  handleAgentNameClick = (e) => {
    this.setState({ currentPage: this.PageEnum.MENU });
  }

  render() {
    const menuItemCb = this.handleItemClick;
    const MainMenu = () => (
      <Menu fluid vertical>
        <Menu.Item onClick={menuItemCb} name='ZMI Structure' />
        <Menu.Item onClick={menuItemCb} name='Queries' />
        <Menu.Item onClick={menuItemCb} name='Contacts' />
      </Menu>
    )
    return (
      <Segment>
        <Header onClick={this.handleAgentNameClick} as='h1'><a>Agent {this.props.agent}</a></Header>
        {(this.state.currentPage === this.PageEnum.MENU) && <MainMenu />}
        {(this.state.currentPage === this.PageEnum.STRUCTURE) && (<Structure />)}
        {(this.state.currentPage === this.PageEnum.QUERIES) && (<p>Queries</p>)}
        {(this.state.currentPage === this.PageEnum.CONTACTS) && (<Contacts agentAddress={this.props.agent} />)}
      </Segment>
    );
  }
}

AgentMenu.propTypes = {
  agent: PropTypes.string.isRequired,
};

export default AgentMenu;
