<template>
  <div class="address-page">
    <h2>收货地址管理</h2>
    <div class="address-list">
      <div v-for="address in addresses" :key="address.id" class="address-card">
        <p><strong>收货人:</strong> {{ address.receiverName }} ({{ address.receiverPhone }})</p>
        <p><strong>地址:</strong> {{ `${address.province} ${address.city} ${address.district} ${address.detail}` }}</p>
      </div>
    </div>
    <div class="add-address-form">
      <h3>新增地址</h3>
      <form @submit.prevent="addNewAddress">
        <input type="text" v-model="newAddress.receiverName" placeholder="收货人姓名" required>
        <input type="text" v-model="newAddress.receiverPhone" placeholder="手机号码" required>
        <input type="text" v-model="newAddress.province" placeholder="省份" required>
        <input type="text" v-model="newAddress.city" placeholder="城市" required>
        <input type="text" v-model="newAddress.district" placeholder="区/县" required>
        <input type="text" v-model="newAddress.detail" placeholder="详细地址" required>
        <button type="submit">添加</button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { getAddressList, addAddress } from '@/api/user';

const addresses = ref([]);
const newAddress = ref({
  receiverName: '',
  receiverPhone: '',
  province: '',
  city: '',
  district: '',
  detail: ''
});

const fetchAddresses = async () => {
  try {
    addresses.value = await getAddressList();
  } catch (error) {
    console.error("获取地址失败:", error);
  }
};

const addNewAddress = async () => {
  try {
    await addAddress(newAddress.value);
    alert('添加成功！');
    fetchAddresses(); 
  
    for (const key in newAddress.value) {
      newAddress.value[key] = '';
    }
  } catch (error) {
    console.error("添加地址失败:", error);
  }
};

onMounted(fetchAddresses);
</script>

<style scoped>
.address-card {
  background: #fff;
  padding: 15px;
  margin-bottom: 10px;
  border-radius: 4px;
  border: 1px solid #eee;
}
.add-address-form {
  background: #fff;
  padding: 20px;
  margin-top: 20px;
  border-radius: 4px;
}
</style>