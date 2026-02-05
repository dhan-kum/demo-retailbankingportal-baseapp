import React, { useState } from 'react';
import axios from 'axios';
import { Button, Modal } from "react-bootstrap";
import BankAccountTransfer from './BankAccountTransfer';

const BankAccountQuickLinks = () => {

  const [modalShow, setmodalShow] = useState(false);

  const showModal = (e) => {
    setmodalShow(false);
  }

  function cancelCLick() {
    setmodalShow(false);
  }

  const hideModal = (e) => {
    setmodalShow(false);
    window.location.reload();
  }

  const showTransferModal = () => {
    setmodalShow(true);
  }

  const getModal = (modalShow) => {
    return (
      <Modal
        show={modalShow}
        onHide={hideModal}
        size="sm"
      >
        <Modal.Header closeButton>
          <Modal.Title
          style={{fontSize:"15px"}}
          >Self Money Transfer</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <BankAccountTransfer cancelCLick={cancelCLick}/>
        </Modal.Body>
      </Modal>
    )
  }

  return (
    <div className='qwrapper'>
      <span className='qSpanHeading'>Quick Links</span>
      <div className='qcontainer'>
        <div className="qcard" onClick={() => showTransferModal()}>
          <i class="fa fa-exchange" aria-hidden="true"></i>
          <span>Transfer</span>
        </div>
        <div className="qcard">
          <i class="fa fa-bars" aria-hidden="true"></i>
          <span>History</span>
        </div>
        <div className="qcard">
          <i class="fa fa-history" aria-hidden="true"></i>
          <span>Limit</span>
        </div>
        <div className="qcard">
          <i class="fa fa-exclamation" aria-hidden="true"></i>
          <span>More</span>
        </div>
      </div>
      {getModal(modalShow)}
    </div>

  );
};

export default BankAccountQuickLinks;
