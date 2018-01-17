import React, { Component } from 'react';
import { Segment, Header, Input, Button } from 'semantic-ui-react';
import PropTypes from 'prop-types';
import axios from 'axios';

class ContactsChangeForm extends Component {
  constructor(props) {
    super(props);
    this.state = {
      newContacts: [],
      contactNameForm: "",
      contactAddressForm: "",
    }
    this.onContactNameChange = this.onContactNameChange.bind(this);
    this.onContactAddressChange = this.onContactAddressChange.bind(this);
    this.onContactAdd = this.onContactAdd.bind(this);
    this.onContactsSubmit = this.onContactsSubmit.bind(this);
  }

  onContactNameChange(event, data) {
    this.setState({ contactNameForm: data.value });
  }

  onContactAddressChange(event, data) {
    this.setState({ contactAddressForm: data.value });
  }

  onContactAdd() {
    const newContact = {
      name: this.state.contactNameForm,
      address: this.state.contactAddressForm,
    }
    let newContacts = this.state.newContacts.slice();
    let contactExists = false;
    newContacts.forEach((ctct) => {
      if (ctct.name === newContact.name) {
        contactExists = true;
      }
    });
    //if (!contactExists) {
      newContacts.push(newContact);
      this.setState({ newContacts: newContacts });
    //}
  }

  onContactsSubmit() {
    this.props.onContactsSubmit(this.state.newContacts);
  }

  render() {
    return (
      <div>
        <Segment>
          <Header as='h3'>Enter new contacts</Header>
          {this.state.newContacts.map((ctct) => (
            <p key={ctct.name}>{ctct.name}: {ctct.address}</p>
          ))}
          <Input onChange={this.onContactNameChange} placeholder='name' />
          <Input onChange={this.onContactAddressChange} placeholder='address' />
          <Button onClick={this.onContactAdd}>Add</Button>
          <div>
            <Button onClick={this.onContactsSubmit} style={{ marginTop: 20 }}>Submit</Button>
          </div>
        </Segment>
      </div>
    )
  }
}


class Contacts extends Component {
  constructor(props) {
    super(props);
    this.state = {
      contacts: [],
    }
    this.fetchContacts = this.fetchContacts.bind(this);
    this.onContactsSubmit = this.onContactsSubmit.bind(this);
  }

  componentWillMount() {
    this.fetchContacts();
  }

  fetchContacts() {
    axios.get(this.props.agentAddress + '/get_fallback').then(res => {
      this.setState({ contacts: res.data.contacts });
    });
  }

  onContactsSubmit(newContacts) {
    axios({
      method: 'post',
      url: this.props.agentAddress + "/set_fallback",
      data: {'contacts': newContacts},
      headers: {'Content-Type': 'application/json'},
    }).then(res => {
      this.fetchContacts();
    })
  }

  render() {
    const contactsState = this.state;
    const ContactsMenu = () => (
      <Segment>
        <Header as='h3'>Current Contacts</Header>
        {contactsState.contacts.map(ctct => {
          return (
            <p
              key={ctct.name}
            >
              {ctct.name + ": " + ctct.address}
            </p>
          );
        })}
      </Segment>
    )
    return (
      <Segment>
        <Header as='h3'>Contacts</Header>
        <ContactsChangeForm
          zone={this.state.changeZoneId}
          onContactsSubmit={this.onContactsSubmit}
        />
        <ContactsMenu />
      </Segment>
    );
  }
}

Contacts.propTypes = {
  agentAddress: PropTypes.string.isRequired,
};

ContactsChangeForm.propTypes = {
  onContactsSubmit: PropTypes.func.isRequired,
}

export default Contacts;
