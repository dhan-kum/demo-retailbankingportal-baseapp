import React, { useState, useEffect } from 'react';
import axios from 'axios';
import DataService from './data'; 

const BankAccountTransactions = () => {
  const [accounts, setAccounts] = useState([]);

  useEffect(() => {
    const fetchAccounts = async () => {
      const dataService = new DataService();
      const response = await dataService.getAccountTransactions();
      setAccounts(response.data);
    };
        
    fetchAccounts();
  }, []);

  return (
    <div className="twrapper">
      <span className='tSpanHeading'>Transaction History</span>
      {/* <span className='tSpanSubheading'><a href="/">Download Statement</a></span> */}
      {accounts.map(account => (
      <div className="tcard">
        <div style={{position: "absolute", top:"0.25rem", left:"0.5rem", fontSize:"14px", fontWeight:"bold"}}>{account.description}</div>
        <div style={{position: "absolute", bottom:"0.25rem", left:"0.5rem", fontSize:"12px"}}>{account.date}</div>
        <div style={{position: "absolute", top:"1rem", left:"15rem", fontSize:"12px" }}>
         { account.paymentType}
        </div>
        <div style={{position: "absolute", top:"1rem", right:"1px", fontSize:"14px", fontWeight:"bold" }}>
          {
          account.type === "debit" ? 
          <span>{ " - " + account.amount}</span>
          : 
          <span style={{color:"green"}}>{ " + " + account.amount}</span>
          }
        </div>
      </div>
      ))}
    </div>
  );
};

export default BankAccountTransactions;
