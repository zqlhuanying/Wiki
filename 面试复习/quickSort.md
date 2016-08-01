```
class　QuickSort
{
  public　void　quickSort(int　arr[],int　low,int　high)
  {
    int　l=low;
    int　h=high;
    int　povit=arr[low];

    // 第一遍快排结束
    while(l<h)
    {
      while(l<h&&arr[h]>=povit)
        h--;
      if(l<h){
        int　temp=arr[h];
        arr[h]=arr[l];
        arr[l]=temp;
        l++;
      }

      while(l<h&&arr[l]<=povit)
        l++;
      if(l<h){
        int　temp=arr[h];
        arr[h]=arr[l];
        arr[l]=temp;
        h--;
      }
    }

    print(arr);
    System.out.print("l="+(l+1)+"h="+(h+1)+"povit="+povit+"\n");
    if(l>low)
      sort(arr,low,l-1);
    if(h<high)
      sort(arr,l+1,high);
  }
}
```
## 利用快排思想求第K大值
1.第一趟快排结束后，获得标兵的下标

2.求得在此标兵之前有多少个元素(L)，若刚好等于 K - 1，则此标兵刚好是第K大元素，直接返回；若大于 k - 1，则说明第K大元素在前半部分；否则在后半部分，则只需要在后半部分钟求第 (K - 1 - L)大元素
