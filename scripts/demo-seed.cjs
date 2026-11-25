/* Local demonstration only. Never prints passwords or writes them into tracked files. */
const fs=require('fs'),crypto=require('crypto');
const env=Object.fromEntries(fs.readFileSync(require('path').resolve(__dirname,'..','.env'),'utf8').split(/\r?\n/).filter(line=>line.includes('=')&&!line.startsWith('#')).map(line=>{const index=line.indexOf('=');return [line.slice(0,index),line.slice(index+1)];}));
const base=process.env.FIADOYA_URL||'http://localhost:4200';
class Client {
 constructor(){this.cookies=new Map();this.csrf='';}
 async request(path,body,method='POST',key){
  const response=await fetch(base+'/api'+path,{method:body===undefined?'GET':method,headers:{'Content-Type':'application/json','Cookie':[...this.cookies].map(([name,value])=>`${name}=${value}`).join('; '),'X-XSRF-TOKEN':this.csrf,...(key?{'Idempotency-Key':key}:{})},body:body===undefined?undefined:JSON.stringify(body)});
  for(const cookie of response.headers.getSetCookie()){const pair=cookie.split(';')[0],index=pair.indexOf('=');this.cookies.set(pair.slice(0,index),pair.slice(index+1));}
  const text=await response.text();let result;try{result=JSON.parse(text);}catch{result=text;}
  if(!response.ok)throw new Error(`${response.status} ${path}: ${result.message||text.slice(0,100)}`);return result;
 }
 async init(){this.csrf=(await this.request('/auth/csrf')).token;}
 async login(email,password){await this.init();return this.request('/auth/login',{email,password});}
}
async function main(){
 fs.mkdirSync('.local',{recursive:true});
 if(fs.existsSync('.local/demo.json')){console.log('Los datos demo ya fueron creados. Se conserva .local/demo.json.');return;}
 const platform=new Client();await platform.login(env.BOOTSTRAP_EMAIL,env.BOOTSTRAP_PASSWORD);
 const credentials=[];
 async function register(name,email){const client=new Client();await client.init();const password=crypto.randomBytes(18).toString('base64url');const user=await client.request('/auth/register',{name,email,password});credentials.push({name,email,password,userId:user.id});return user;}
 const bodegaAdmin=await register('Ana Torres · Bodega','bodega@fiadoya.test');
 const salonAdmin=await register('Lucía Ramos · Salón','salon@fiadoya.test');
 const customerUser=await register('Juan Alberto Pérez Gómez','juan@fiadoya.test');
 const stores=[];
 for(const [name,businessType,admin]of [['Bodega El Vecino','Bodega',bodegaAdmin],['Salón Nueva Imagen','Peluquería',salonAdmin]]){const store=await platform.request('/stores',{name,businessType,address:'Dirección ficticia para demostración',phone:'',active:true});await platform.request(`/stores/${store.id}/admins/${admin.id}`,{});stores.push(store);}
 const rate={percent:24,type:'NOMINAL',periodDays:360,capitalizationDays:30};
 const terms={currency:'PEN',creditLimit:1500,maxMonths:6,cutoffDay:15,cutoffTime:'23:59',paymentDay:25,compensatory:rate,penalty:{percent:35,type:'EFFECTIVE',periodDays:360,capitalizationDays:0}};
 const accounts=[];
 for(const [index,firstName,lastName,doc,user]of [[0,'Juan Alberto','Pérez Gómez','72839401',customerUser],[0,'María','Quispe Rojas','72839402',null],[1,'Pedro','Salas Vega','72839403',null],[1,'Juan Alberto','Pérez Gómez','72839401',customerUser]]){
  const result=await platform.request(`/stores/${stores[index].id}/customers`,{userId:user?.id??null,documentType:'DNI',documentNumber:doc,firstName,lastName,email:user?.email??'',phone:'',address:'',consent:!!user,active:true,terms:index===1?{...terms,compensatory:{percent:2,type:'EFFECTIVE',periodDays:30,capitalizationDays:0}}:terms});accounts.push(result.account);
 }
 const products=[];
 for(const [index,code,name,price]of [[0,'AR-01','Arroz extra · saco',52],[0,'GA-01','Balón de gas',60],[0,'AC-01','Aceite vegetal · botella',12],[1,'CO-01','Corte de cabello',30],[1,'TR-01','Tratamiento capilar',80]]){
  products.push(await platform.request(`/stores/${stores[index].id}/products`,{code,name,description:'Producto ficticio de demostración',supplier:'',brand:'',unit:index===0?'unidad':'servicio',imageUrl:'',cashPrice:price,creditPrice:price+3,currency:'PEN',mode:'BOTH',active:true}));
 }
 await platform.request('/clock',{dateTime:'2026-10-06T10:38:00'});
 const purchases=[];
 purchases.push(await platform.request(`/stores/${stores[0].id}/sales`,{accountId:accounts[0].id,mode:'INSTALLMENTS',months:3,items:[{productId:products[0].id,quantity:2},{productId:products[1].id,quantity:1}]},'POST','demo-academic-purchase-01'));
 purchases.push(await platform.request(`/stores/${stores[0].id}/sales`,{accountId:accounts[1].id,mode:'MONTH_END',months:1,items:[{productId:products[2].id,quantity:3}]},'POST','demo-month-end-purchase-01'));
 purchases.push(await platform.request(`/stores/${stores[1].id}/sales`,{accountId:accounts[2].id,mode:'MONTH_END',months:1,items:[{productId:products[3].id,quantity:1}]},'POST','demo-salon-purchase-01'));
 purchases.push(await platform.request(`/stores/${stores[1].id}/sales`,{accountId:accounts[3].id,mode:'INSTALLMENTS',months:2,items:[{productId:products[4].id,quantity:1}]},'POST','demo-salon-purchase-02'));
 await platform.request('/clock',{dateTime:'2026-10-25T10:00:00'});
 const quote=await platform.request(`/stores/${stores[1].id}/accounts/${accounts[2].id}/quote`);
 await platform.request(`/stores/${stores[1].id}/payments`,{accountId:accounts[2].id,received:quote.total,method:'CASH'},'POST','demo-salon-payment-01');
 await platform.request('/clock',{dateTime:'2026-12-06T10:00:00'});
 for(const store of stores)await platform.request(`/stores/${store.id}/cycles/close`,{});
 fs.writeFileSync('.local/demo-credentials.json',JSON.stringify(credentials,null,2));
 fs.writeFileSync('.local/demo.json',JSON.stringify({stores,accounts,purchases},null,2));
 console.log('Demostración creada: 2 tiendas, 4 cuentas, 5 productos, 4 compras y 1 pago. Credenciales locales en .local/demo-credentials.json.');
}
if(require.main===module)main().catch(error=>{console.error(error.message);process.exitCode=1;});
module.exports={Client};
