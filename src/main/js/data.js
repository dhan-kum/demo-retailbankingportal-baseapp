class DataService {

async  getAccountDetails(){
    return  (
     {
     "data" :[
        {
            "id": 1,
            "accountHolderName":"John Doe",   
            "accountNumber": "008596512563",
            "type": "Savings",
            "balance": "52000"
        },
        {
            "id": 2,
            "accountHolderName":"John Doe",
            "accountNumber": "008596558965",
            "type": "Checking",
            "balance": "7500"
        }
    ]
     ,"status" : 200
     }
     )
   };

   async  getAccountTransactions(){
    return  (
     {
     "data" :[
        {
            "id": 1,
            "date": "01 Apr 2023 at 02:15 PM",
            "amount": "8,500.00 $",
            "description": "Google Inc.",
            "paymentType":"Transfer",
            "type":"credit"
        },
        {
            "id": 2,
            "date": "25 Mar 2023 at 07:06 AM",
            "amount": "15.00 $",
            "description": "Starbucks Cafe",
            "paymentType":"Card Payment",
            "type":"debit"
        },
        {
            "id": 3,
            "date": "18 Mar 2023 at 09:23 AM",
            "amount": "10.00 $",
            "description": "Spotify Premium",
            "paymentType":"Fee",
            "type":"debit"
        },
        {
            "id": 4,
            "date": "12 Mar 2023 at 08:32 PM",
            "amount": "20.00 $",
            "description": "Mcdonalds",
            "paymentType":"Card Payment",
            "type":"debit"
        },
        {
            "id": 5,
            "date": "05 Mar 2023 at 07:52 PM",
            "amount": "18.00 $",
            "description": "Carefour Express",
            "paymentType":"Card Payment",
            "type":"debit"
        }
    ]
     ,"status" : 200
     }
     )
   };
}
export default DataService;