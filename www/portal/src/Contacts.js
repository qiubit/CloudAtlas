import React, { Component } from 'react';
import { Segment, Header, Menu, Input, Button } from 'semantic-ui-react';
import PropTypes from 'prop-types';
import axios from 'axios';

class ContactsChangeForm extends Component {
  constructor(props) {
    super(props);
    this.state = {
      newContacts: "",
    }
    this.onContactsChange = this.onContactsChange.bind(this);
    this.onContactsSubmit = this.onContactsSubmit.bind(this);
  }

  onContactsChange(event, data) {
    this.setState({ newContacts: data.value });
  }

  onContactsSubmit() {
    this.props.onChangeRequest(this.state.newContacts);
  }

  render() {
    return (
      <div>
        <p>Enter new zone {this.props.zone} contacts:</p>
        <Input onChange={this.onContactsChange} placeholder='contact1, contact2, contact3'/>
        <Button onClick={this.onContactsSubmit}>Change</Button>
      </div>
    )
  }
}


class Contacts extends Component {
  constructor(props) {
    super(props);
    this.state = {
      contacts: [],
      changeZoneId: null,
    }
    this.addZoneContacts = this.addZoneContacts.bind(this);
    this.onEnterZoneContactsChange = this.onEnterZoneContactsChange.bind(this);
    this.onChangeRequest = this.onChangeRequest.bind(this);
  }

  addZoneContacts(zoneId, zoneContacts) {
    let newContactsArr = this.state.contacts.slice();
    newContactsArr.push({ id: zoneId, contacts: zoneContacts });
    console.log(newContactsArr);
    this.setState({ contacts: newContactsArr });
  }

  fetchZoneContacts(zones) {
    for (let i = 0; i < zones.length; i++) {
      const zoneId = zones[i];
      axios.get(this.props.agentAddress + "/contacts/" + zoneId).then(res => {
        console.log(res.data);
        this.addZoneContacts(zoneId, res.data.contacts);
      })
    }
  }

  onEnterZoneContactsChange = (zoneId) => () => {
    this.setState({ contacts: [], changeZoneId: zoneId })
  }

  onChangeRequest(newContacts) {
    const arr = newContacts.split(',');
    axios.put(this.props.agentAddress + "/contacts/" + this.state.changeZoneId, { contacts: arr });
  }

  componentDidMount() {
    axios.get(this.props.agentAddress + "/zones").then(res => {
      this.fetchZoneContacts(res.data.zones);
    })
  }

  render() {
    const contactsState = this.state;
    const ContactsMenu = () => (
      <Menu fluid vertical>
        {contactsState.contacts.map(zoneData => {
          return (
            <Menu.Item
              key={zoneData.id}
              onClick={this.onEnterZoneContactsChange(zoneData.id)}
              name={zoneData.id.toString()}>{zoneData.id.toString() + ": " + zoneData.contacts.toString()}
            </Menu.Item>
          );
        })}
      </Menu>
    )
    return (
      <Segment>
        <Header as='h3'>Contacts</Header>
        {this.state.changeZoneId == null && <ContactsMenu />}
        {this.state.changeZoneId != null && (
          <ContactsChangeForm
            zone={this.state.changeZoneId}
            onChangeRequest={this.onChangeRequest}
          />
        )}
      </Segment>
    );
  }
}

Contacts.propTypes = {
  agentAddress: PropTypes.string.isRequired,
};

ContactsChangeForm.propTypes = {
  onChangeRequest: PropTypes.func.isRequired,
}

export default Contacts;
