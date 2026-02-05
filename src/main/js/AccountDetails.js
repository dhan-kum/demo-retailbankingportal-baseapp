import React, { useEffect, useState } from 'react';
import axios from 'axios';

const AccountDetails = () => {
  const [accountDetails, setAccountDetails] = useState([]);

  useEffect(() => {
    axios.get('/api/bankaccounts/')
      .then(response => {
        setAccountDetails(response.data);
      })
      .catch(error => {
        console.log(error);
      });
  }, []);

  return (
    <div className="container">
      <h1>Bank Account Details</h1>
      <table className="table">
        <thead>
          <tr>
            <th>Account Number</th>
            <th>Account Name</th>
            <th>Balance</th>
          </tr>
        </thead>
        <tbody>
          {accountDetails.map(account => (
            <tr key={account.id}>
              <td>{account.accountNumber}</td>
              <td>{account.accountName}</td>
              <td>{account.balance}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default AccountDetails;
