import {
  Tabbar,
  TabbarItem,
  Button,
  Form,
  Field,
  Col,
  Row,
  Popup,
  Calendar,
  Checkbox,
  CheckboxGroup,
  DatetimePicker,
  NumberKeyboard,
  PasswordInput,
  Picker,
  RadioGroup, Radio,
  Search,
  Uploader,
  ActionSheet,
  Dialog,Tab,Tabs,
  DropdownMenu, DropdownItem,
  Toast, PullRefresh,
  NavBar,
  Notify,List,
  Grid, GridItem,
  Skeleton, Sticky,
  Divider, Icon,Tag,
  Empty, Cell, CellGroup, Switch
} from 'vant';

const initVant = Vue => {
  Vue.use(Tabbar).use(List).use(Tag)
    .use(Button).use(Form).use(TabbarItem)
    .use(Field).use(Col).use(Row).use(Divider)
    .use(Popup).use(Calendar).use(Checkbox)
    .use(CheckboxGroup).use(DatetimePicker)
    .use(NumberKeyboard).use(PasswordInput)
    .use(Picker).use(RadioGroup).use(Radio)
    .use(Search).use(Uploader).use(ActionSheet)
    .use(Dialog).use(DropdownMenu).use(DropdownItem)
    .use(Toast).use(PullRefresh).use(NavBar)
    .use(Grid).use(GridItem).use(Skeleton)
    .use(Icon).use(Empty).use(Cell).use(CellGroup)
    .use(Switch).use(Sticky).use(Tab).use(Tabs);
}

export default initVant


