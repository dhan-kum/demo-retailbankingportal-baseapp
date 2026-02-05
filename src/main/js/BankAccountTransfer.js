import React, { useState, useEffect } from 'react';
import axios from 'axios';

function BankAccountTransfer() {
  const [senderAccount, setSenderAccount] = useState('');
  const [receiverAccount, setReceiverAccount] = useState('');
  const [amount, setAmount] = useState('');
  const [accounts, setAccounts] = useState([]);

  useEffect(() => {
    async function fetchAccounts() {
      try {
        const response = await axios.get('http://localhost:8080/api/bankaccounts/');
        setAccounts(response.data);
      } catch (error) {
        alert(`Failed to fetch accounts: ${error.response.data}`);
      }
    }
    fetchAccounts();
  }, []);

  const handleSubmit = async (event) => {
    event.preventDefault();
    try {
      await axios.post('http://localhost:8080/api/bankaccounts/transfer', { senderAccount: senderAccount, receiverAccount: receiverAccount, amount });
      alert('Transfer successful');
    } catch (error) {
      alert(`Transfer failed: ${error.response.data}`);
    }    
  };

  return (
    <form onSubmit={handleSubmit}>
      <div className="form-group">
        <label htmlFor="from-account">From Account:</label>
        <select
          id="from-account"
          className="form-control"
          value={senderAccount}
          onChange={(e) => setSenderAccount(e.target.value)}
        >
          <option value="">Select an account</option>
          {accounts.map((account) => (
            <option key={account.id} value={account.accountNumber}>
              {account.accountNumber} (${account.balance})
            </option>
          ))}
        </select>
      </div>
      <div className="form-group">
        <label htmlFor="to-account">To Account:</label>
        <select
          id="to-account"
          className="form-control"
          value={receiverAccount}
          onChange={(e) => setReceiverAccount(e.target.value)}
        >
          <option value="">Select an account</option>
          {accounts.map((account) => (
            <option key={account.id} value={account.accountNumber}>
              {account.accountNumber} (${account.balance})
            </option>
          ))}
        </select>
      </div>
      <div className="form-group">
        <label htmlFor="amount">Amount($):</label>
        <input
          type="text"
          id="amount"
          className="form-control"
          value={amount}
          onChange={(e) => setAmount(e.target.value)}
        />
      </div>
      <br/>
      <button type="submit" className="submit">Transfer</button>
    </form>
  );
}

export default BankAccountTransfer;
